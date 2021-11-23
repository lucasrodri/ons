package ons;

public class WDMOXC extends OXC {

    protected int wvlConverters;
    protected int freeWvlConverters;
    protected int wvlConversionRange;

    public WDMOXC(int id, int groomingInPorts, int groomingOutPorts, int type, int group, int wvlConverters, int wvlConversionRange) {
        super(id, groomingInPorts, groomingOutPorts, type, group);
        this.wvlConverters = this.freeWvlConverters = wvlConverters;
        this.wvlConversionRange = wvlConversionRange;
    }

    /**
     * This function says whether or not a given OXC has free wavelength
     * converter(s).
     *
     * @return true if the OXC has free wavelength converter(s)
     */
    public boolean hasFreeWvlConverters() {
        return freeWvlConverters > 0;
    }

    /**
     * By decreasing the number of free wavelength converters, this function
     * "reserves" a wavelength converter.
     *
     * @return false if there are no free wavelength converters
     */
    public boolean reserveWvlConverter() {
        if (freeWvlConverters > 0) {
            freeWvlConverters--;
            return true;
        } else {
            return false;
        }
    }

    /**
     * By increasing the number of free wavelength converters, this function
     * "releases" a wavelength converters.
     *
     * @return false if there are no wavelength converters to be freed
     */
    public boolean releaseWvlConverter() {
        if (freeWvlConverters < wvlConverters) {
            freeWvlConverters++;
            return true;
        } else {
            return false;
        }
    }

    /**
     * This function provides the wavelength conversion range of a given OXC.
     *
     * @return the OXC's wvlConversionRange attribute
     */
    public int getWvlConversionRange() {
        return wvlConversionRange;
    }

}
