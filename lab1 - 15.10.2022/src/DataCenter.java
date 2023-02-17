public class DataCenter {
    public static int getCommunicatingServersCount(int[][] map) {
        int serversCount = 0;

        for (int i = 0; i < map.length; ++i) {
            for (int j = 0; j < map[i].length; ++j) {

                boolean flag = false;

                if (map[i][j] == 1) {
                    for (int k = 0; !flag && k < map[i].length; ++k) {
                        if (k != j && map[i][k] == 1) {
                            ++serversCount;
                            flag = true;
                        }
                    }

                    for (int k = 0; !flag && k < map.length; ++k) {
                        if (k != i && map[k][j] == 1) {
                            ++serversCount;
                            flag = true;
                        }
                    }
                }
            }
        }

        return serversCount;
    }
}
