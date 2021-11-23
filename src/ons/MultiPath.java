/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

/**
 *
 * @author lucasrc
 */
public class MultiPath {

    private Path[] paths;

    public MultiPath(Path[] paths) {
        this.paths = paths;
    }

    public Path[] getPaths() {
        return paths;
    }

    public void setPaths(Path[] paths) {
        this.paths = paths;
    }

    public Path getPath(int num) {
        if (num >= paths.length) {
            throw (new IllegalArgumentException());
        } else {
            return paths[num];
        }
    }

    public int getTotalBWAvailable() {
        int bw = 0;
        for (Path path : paths) {
            bw += path.getBWAvailable();
        }
        return bw;
    }

    public int getTotalBW() {
        int bw = 0;
        for (Path path : paths) {
            bw += path.getBW();
        }
        return bw;
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < paths.length; i++) {
            str += "Path-" + i + ": \n" + paths[i].toString();
        }
        return str;
    }

    /**
     * Retrieves if this MultiPath has this lightpath or not.
     * @param lp the lightpath
     * @return true if this MultiPath has this lightpath or not, false otherwise
     */
    boolean hasLightpath(LightPath lightpath) {
        for (Path path : paths) {
            if(path.hasLightpath(lightpath)) {
                return true;
            }
        }
        return false;
    }
}
