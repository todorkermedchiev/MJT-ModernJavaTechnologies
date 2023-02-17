import java.lang.reflect.Array;

public class PrefixExtractor {
    public static String getLongestCommonPrefix(String[] words) {
        StringBuilder prefix = new StringBuilder("");

        if (words == null || words.length == 0) {
            return prefix.toString();
        }

        int shortest = words[0].length();
        for (int i = 0; i < words.length; ++i) {
            if (words[i].length() < shortest) {
                shortest = words[i].length();
            }
        }

        boolean flag = true;
        for (int i = 0; flag && i < shortest; ++i) {
            for (int j = 0; j < words.length - 1; ++j) {
                if (words[j].charAt(i) != words[j + 1].charAt(i)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                prefix.append(words[0].charAt(i));
            }
        }

        return prefix.toString();
    }
}