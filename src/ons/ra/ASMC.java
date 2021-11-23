/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ons.ra;

import java.util.Arrays;
import java.util.HashMap;

import ons.EONLightPath;
import ons.EONLink;
import ons.EONPhysicalTopology;
import ons.Flow;
import ons.LightPath;
import ons.Modulation;
import ons.util.Dijkstra;
import ons.util.WeightedGraph;

/**
 * This is a new algorithm to solve the RMSCA problem in SDM-EON.
 * The algorithm tries to allocate circuits with resistant modulation on
 * the central core, and separate the circuits in odd and even on peripheral
 * cores, to reduce fragmentation.
 */
public class ASMC implements RA {

	private ControlPlaneForRA cp;
	private WeightedGraph graph;
	private int modulation;

	@Override
	public void flowArrival(Flow flow) {

		int[] nodes = null, links = null;
		HashMap<String, int[]> result;
		int requiredSlots;
		long id;
		LightPath[] lps = new LightPath[1];

		// Shortest-Path routing
		nodes = Dijkstra.getShortestPath(graph, flow.getSource(), flow.getDestination());

		// If no possible path found, block the call
		if (nodes.length == 0) {
			cp.blockFlow(flow.getID()); 
			return;
		}

		// Create the links vector
		links = new int[nodes.length - 1];
		for (int j = 0; j < nodes.length - 1; j++) {
			links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
		}

		result = selectModulationCoreSlotByXT(flow, cp, links);

		// if no solution is found, the circuit is blocked
		if(result == null)
		{
			cp.blockFlow(flow.getID());
			return;
		}else
		{
			modulation = result.get("mod")[0];
			requiredSlots = Modulation.convertRateToSlot(flow.getRate(), 
					EONPhysicalTopology.getSlotSize(), result.get("mod")[0]);
			
			EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
					result.get("cores"), result.get("slot")[0], (result.get("slot")[0] + requiredSlots - 1), modulation);
			
			if ((id = cp.getVT().createLightpath(lp)) >= 0) {
				lps[0] = cp.getVT().getLightpath(id);
				if (!cp.acceptFlow(flow.getID(), lps)) {
					cp.getVT().deallocatedLightpaths(lps);
				} else {
					return;
				}
			} 
		}
		
		// Block the call
		cp.blockFlow(flow.getID());
	}

	/**
	 * This method verifies all the available modulations and return
	 * a hash <String, int[]> containing the elements:
	 *  
	 * "mod" = int[0] - chosen modulation
	 * "slot" = int[0] - chosen slot
	 * "cores" = int[links.length] - list of chosen cores
	 * the viable and most complex modulation format [0], with the chosen core [1] and the 
	 * chosen slot [2]
	 * 
	 * @param flow evaluated circuit
	 * @param cp control plane
	 * @param routeLinks list of route links
	 * @param totalDistance 
	 * @return hashmap<String, int[]>.
	 */
	private static HashMap<String, int[]> selectModulationCoreSlotByXT(Flow flow, ControlPlaneForRA cp, int[] routeLinks) 
	{
		HashMap <String, int[]> modSlotCores = new HashMap<String, int[]>();
		int modEscolhida = -1, slotEscolhido = -1, requiredSlots;
		int[] coresEscolhido = cleanVector(routeLinks.length);

		// int[0] chosen slot, 
		// int[1] to int[links.length] chosen cores
		int[] coreAndSlot;

		// Keeps the modulation (Integer) with the slot ('int[0]')  
		// and chosen cores ('int[1] - int[links.length']).
		HashMap<Integer, int[]> coreAndSlotPerModulation = new HashMap<Integer, int[]>();

		// chose the slots and core for each modulation format, considering the current network state
		for(int mod = Modulation._BPSK; mod <= EONPhysicalTopology.getMaxModulation(); mod++)
		{
			requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), mod);
			coreAndSlot = searchCoreAndSlotPerModulation(cp, flow, routeLinks, requiredSlots, mod);
			if(coreAndSlot != null)
			{
					coreAndSlotPerModulation.put(mod, coreAndSlot);
			}
		}

		if(coreAndSlotPerModulation.isEmpty())
		{
			return null;
		}

		// the most complex modulation is chosen (which is viable and attends the crosstalk requirement)
		for(Integer modAtual : coreAndSlotPerModulation.keySet())
		{
			int[] slotCores = coreAndSlotPerModulation.get(modAtual);

			if(slotCores == null)
				continue;

			// select the most complex format
			if (modAtual > modEscolhida) 
			{
				modEscolhida = modAtual;
				coresEscolhido = Arrays.copyOfRange(slotCores, 1, routeLinks.length + 1);
				slotEscolhido = slotCores[0];
			}
		}

		// when is possible, select the most robust modulation which have the same number of slots
		if(modEscolhida != -1)
		{
			int requiredSlotsAtual = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modEscolhida);

			for(Integer modAtual : coreAndSlotPerModulation.keySet())
			{
				int[] slotCores = coreAndSlotPerModulation.get(modAtual);
				int tempRequiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modAtual);

				if(slotCores == null)
					continue;

				// changes the modulation if the slot number is the same
				if ((modAtual < modEscolhida) && (requiredSlotsAtual == tempRequiredSlots)) 
				{
					modEscolhida = modAtual;
					coresEscolhido = Arrays.copyOfRange(slotCores, 1, routeLinks.length + 1);;
					slotEscolhido = slotCores[0];
				}
			}
		}

		if(modEscolhida != -1)
		{
			int[] mod = new int[1], slot = new int[1];
			mod[0] = modEscolhida;
			slot[0] =slotEscolhido;

			modSlotCores.put("mod", mod);
			modSlotCores.put("cores", coresEscolhido);
			modSlotCores.put("slot", slot);
		}else
			return null;
		
		return modSlotCores;
	}

	/**
	 * This method chooses the core and the first slot for the route 'routeLinks'.
	 * The algorithm allocates the most resistant circuits (BPSK and QPSK) with First Fit in central core. 
	 * The cores are dedicated for odd or even requests (reduce fragmentation) that are not allocated in central core.
	 * In even cores are allocated requests which have even number of slots. 
	 * In odd cores are allocated requests which have odd number of slots.
	 * @param cp2
	 * @param routeLinks
	 * @param requiredSlots
	 * @return int[0] slot int[1] - int[links.length] list of cores chosen by route link.
	 */
	private static int[] searchCoreAndSlotPerModulation(ControlPlaneForRA cp, Flow flow, int[] routeLinks, int requiredSlots, int modulation) 
	{
		//TODO change for different types of fiber
		int nucleoCentral = 6;
		int[] slotCores = cleanVector(routeLinks.length + 1);
		int maxSlots = ((EONLink) cp.getPT().getLink(0)).getNumSlots(0);

		// if modulation is BPSK or QPSK, tries to allocate firstly on central core
		if(modulation == Modulation._BPSK || modulation == Modulation._QPSK) 
		{ // get the allocable slots on first core
			slotCores = searchCoreAndSlot(flow, cp, routeLinks, requiredSlots, nucleoCentral, modulation);
		}

		// To all the other cases, allocate with FF circuits with even number of slots on even cores
		// and circuits with odd number of slots on odd cores.
		if(slotCores[0] == -1 || slotCores[1] == -1)
		{ // search available cores (even or odd) according to 'requiredSlots'.
			slotCores = searchOddEvenCoreSlot(cp, flow, routeLinks, requiredSlots, modulation);
		}

		boolean nucleosEncontrados = false;

		// if no resources are found, searches in the common area.
		// The common area are the slots on the end of the spectrum, in all cores.
		// The most LF slot is selected.
		if(slotCores[0] == -1 || slotCores[1] == -1)
		{	
			for(int s = maxSlots - requiredSlots; s >= 0; s--)
			{
				for(int l = 0; l < routeLinks.length; l++)
				{
					EONLink linkAtual = (EONLink) cp.getPT().getLink(routeLinks[l]);
					nucleosEncontrados = false;
					for(int c = 0; c < linkAtual.getNumCores(); c++)
					{
						// verifies if 'slot' is free on core 'c' of link 'l'.
						if(linkAtual.areSlotsAvaiable(c, s, s + requiredSlots - 1))
						{
							slotCores[l+1] = c;
							nucleosEncontrados = true;
							break;
						}
					}
					if(!nucleosEncontrados)
					{
						slotCores = cleanVector(routeLinks.length + 1);
						break;
					}
				}
				if(nucleosEncontrados)
				{
					slotCores[0] = s;
					int[] coresEscolhidos = Arrays.copyOfRange(slotCores, 1, routeLinks.length + 1);
					
					EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), routeLinks,
							coresEscolhidos, s, (s + requiredSlots - 1), modulation);

					if (!cp.getPT().canCreatePhysicalLightpath(lp))
					{
						slotCores = cleanVector(routeLinks.length + 1);
						continue;
					}
					else
						break;
				}
			}
		}
		
		if(slotCores[0] == -1)
			return null;
		return slotCores;
	}

	/**
	 * Verifies the viable slots to 'firstfit' allocation of the entire route,
	 * presented as 'routeLinks', considering 'requiredSlots' contiguous slots,
	 * allocated only on core 'core'. 
	 * @param cp
	 * @param routeLinks current route links
	 * @param requiredSlots
	 * @param core
	 * @return
	 */
	private static int[] searchCoreAndSlot(Flow flow, ControlPlaneForRA cp, int[] routeLinks, 
			int requiredSlots, int core, int modulation) 
	{
		int[] slotCores = cleanVector(routeLinks.length + 1);
		int[] slotsAlocaveis = ((EONLink) cp.getPT().getLink(routeLinks[0])).firstFit(core, requiredSlots);
		
		// create list with cores defined for allocation
		int[] selectedCores = new int[routeLinks.length];
		for(int c = 0; c < selectedCores.length; c++)
		{
			selectedCores[c] = core;
		}

		for(int slot : slotsAlocaveis)
		{
			EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), routeLinks, 
					selectedCores, slot, (slot + requiredSlots - 1), modulation);

			if (!cp.getPT().canCreatePhysicalLightpath(lp))
				continue;
			else
			{
				slotCores[0] = slot;
				// chose the central core for all the route links
				for(int c = 1; c <= routeLinks.length; c++)
				{
					slotCores[c] = core;
				}
				break;
			}
		}
		return slotCores;
	}

	/**
	 * Verifies the viable slots to 'first fit' alocation of the whole route 
	 * (defined as 'routeLinks'), considering 'requiredSlots' contiguous slots, 
	 * allocating only on odd (or even) cores, according to the number of slots (odd or even).
	 * @param cp
	 * @param routeLinks links que compoem a rota atual
	 * @param requiredSlots
	 * @return int[0] slot, int[1] - int[routeLinks.length] nucleos escolhidos por enlace
	 */
	private static int[] searchOddEvenCoreSlot(ControlPlaneForRA cp, Flow flow, int[] routeLinks, 
			int requiredSlots, int modulation) 
	{
		int[] slotCores = new int[routeLinks.length + 1], selectedCores = new int[3];
		int maxSlots = ((EONLink) cp.getPT().getLink(0)).getNumSlots(0);
		boolean nucleosEncontrados = false;

		if(requiredSlots % 2 == 0)
		{ // search on even cores if 'requiredSlots' is even
			selectedCores[0] = 0;
			selectedCores[1] = 2;
			selectedCores[2] = 4;

		}else
		{ // search on odd cores if 'requiredSlots' is odd
			selectedCores[0] = 1;
			selectedCores[1] = 3;
			selectedCores[2] = 5;
		}

		// search the slot
		for(int s = 0; s < maxSlots - requiredSlots + 1; s++)
		{
			for(int l = 0; l < routeLinks.length; l++)
			{
				nucleosEncontrados = false;
				for(int c = 0; c < selectedCores.length; c++)
				{
					// verifies if 'slot' is free and can be allocated on core 'c' of link 'l'
					if(((EONLink) cp.getPT().getLink(l)).areSlotsAvaiable(selectedCores[c], s, s + requiredSlots - 1))
					{
						slotCores[l+1] = selectedCores[c];
						nucleosEncontrados = true;
						break;
					}
				}
				if(!nucleosEncontrados)
				{
					slotCores = cleanVector(routeLinks.length + 1);
					break;
				}
			}
			
			if(nucleosEncontrados)
			{
				slotCores[0] = s;
				int[] coresEscolhidos = Arrays.copyOfRange(slotCores, 1, routeLinks.length + 1);
				

				EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), routeLinks,
						coresEscolhidos, s, (s + requiredSlots - 1), modulation);

				if (!cp.getPT().canCreatePhysicalLightpath(lp))
				{
					slotCores = cleanVector(routeLinks.length + 1);
					continue;
				}
				else
					break;
			}
		}
		return slotCores;
	}

	/**
	 * Creates and returns a vector with size 'length',
	 * with all the elements setted as -1.
	 * @param length vector size
	 * @return int[] vector with size 'length'.
	 */
	private static int[] cleanVector(int length) {
		int[] vetor = new int[length];

		for (int i = 0; i < length; i++) {
			vetor[i] = -1;
		}
		return vetor;
	}

	@Override
	public void flowDeparture(long id) {
	}

	@Override
	public void simulationEnd() {
	}

	@Override
	public void simulationInterface(ControlPlaneForRA cp) {
		this.cp = cp;
		this.graph = cp.getPT().getWeightedGraph();
		//The default modulation
		this.modulation = Modulation._BPSK;
	}

	@Override
	public void setModulation(int modulation) {
		this.modulation = modulation;
	}
}
