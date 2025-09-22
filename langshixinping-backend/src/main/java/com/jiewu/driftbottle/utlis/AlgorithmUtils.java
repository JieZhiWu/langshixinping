package com.jiewu.driftbottle.utlis;

import java.util.*;

/**
* 算法工具类
*/
public class AlgorithmUtils {

    /**
* 编辑距离算法（用于计算最相似的两组标签）
* 原理：https://blog.csdn.net/DBC_121/article/details/104198838
*
* @param tagList1
* @param tagList2
* @return
*/
    public static int minDistance(List<String> tagList1, List<String> tagList2) {
        int n = tagList1.size();
        int m = tagList2.size();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (!Objects.equals(tagList1.get(i - 1), tagList2.get(j - 1))) {
                    left_down += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }
        return d[n][m];
    }

    /**
* 编辑距离算法（用于计算最相似的两个字符串）
* 原理：https://blog.csdn.net/DBC_121/article/details/104198838
*
* @param word1
* @param word2
* @return
*/
    public static int minDistance(String word1, String word2) {
        int n = word1.length();
        int m = word2.length();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (word1.charAt(i - 1) != word2.charAt(j - 1)) {
                    left_down += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }
        return d[n][m];
    }
    /**
     * 余弦相似度算法（用于计算两组标签的相似度）
     * 原理：将标签列表转换为词袋模型向量，再计算余弦相似度
     *
     * @param tagList1 第一组标签
     * @param tagList2 第二组标签
     * @return 相似度值（0-1之间）
     */
    public static double cosineSimilarity(List<String> tagList1, List<String> tagList2) {
        // 处理空列表情况
        if (tagList1 == null || tagList2 == null || tagList1.isEmpty() || tagList2.isEmpty()) {
            return 0.0;
        }

        // 合并所有标签，获取唯一标签集合（词袋）
        Set<String> allTags = new HashSet<>(tagList1);
        allTags.addAll(tagList2);
        List<String> uniqueTags = new ArrayList<>(allTags);

        // 构建词频向量
        int[] vector1 = getTermFrequencyVector(tagList1, uniqueTags);
        int[] vector2 = getTermFrequencyVector(tagList2, uniqueTags);

        // 计算点积
        double dotProduct = 0.0;
        for (int i = 0; i < uniqueTags.size(); i++) {
            dotProduct += vector1[i] * vector2[i];
        }

        // 计算向量模长
        double normA = calculateNorm(vector1);
        double normB = calculateNorm(vector2);

        // 避免除以零
        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        // 返回余弦相似度
        return dotProduct / (normA * normB);
    }

    /**
     * 余弦相似度算法（用于计算两个字符串的相似度）
     * 基于字符级别的词袋模型
     *
     * @param word1 第一个字符串
     * @param word2 第二个字符串
     * @return 相似度值（0-1之间）
     */
    public static double cosineSimilarity(String word1, String word2) {
        // 处理空字符串情况
        if (word1 == null || word2 == null || word1.isEmpty() || word2.isEmpty()) {
            return 0.0;
        }

        // 提取所有唯一字符
        Set<Character> allChars = new HashSet<>();
        for (char c : word1.toCharArray()) {
            allChars.add(c);
        }
        for (char c : word2.toCharArray()) {
            allChars.add(c);
        }
        List<Character> uniqueChars = new ArrayList<>(allChars);

        // 构建字符频率向量
        int[] vector1 = getCharFrequencyVector(word1, uniqueChars);
        int[] vector2 = getCharFrequencyVector(word2, uniqueChars);

        // 计算点积
        double dotProduct = 0.0;
        for (int i = 0; i < uniqueChars.size(); i++) {
            dotProduct += vector1[i] * vector2[i];
        }

        // 计算向量模长
        double normA = calculateNorm(vector1);
        double normB = calculateNorm(vector2);

        // 避免除以零
        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        // 返回余弦相似度
        return dotProduct / (normA * normB);
    }

    /**
     * 生成标签列表的词频向量
     */
    /*
    // 原始标签列表
    List<String> tagList = Arrays.asList("java", "开发", "java", "后端");
    // 所有唯一标签（词袋）
    List<String> uniqueTags = Arrays.asList("java", "开发", "后端", "前端");

    // 生成词频向量
    int[] vector = getTermFrequencyVector(tagList, uniqueTags);

    // 输出结果
        System.out.println("词频向量：" + Arrays.toString(vector));
    // 输出：[2, 1, 1, 0]
    // 含义："java"出现2次，"开发"出现1次，"后端"出现1次，"前端"出现0次
*/
    private static int[] getTermFrequencyVector(List<String> tagList, List<String> uniqueTags) {
        int[] vector = new int[uniqueTags.size()];
        for (int i = 0; i < uniqueTags.size(); i++) {
            vector[i] = Collections.frequency(tagList, uniqueTags.get(i));
        }
        return vector;
    }

    /**
     * 生成字符串的字符频率向量
     */
    /*
    // 原始字符串
    String word = "编程编程java";
    // 提取所有唯一字符
    Set<Character> charSet = new HashSet<>();
        for (char c : word.toCharArray()) {
        charSet.add(c);
    }
    List<Character> uniqueChars = new ArrayList<>(charSet);
    // 此处uniqueChars可能为：['编', '程', 'j', 'a', 'v']（顺序不固定）

    // 生成字符频率向量
    int[] vector = getCharFrequencyVector(word, uniqueChars);

    // 输出结果
        System.out.println("字符频率向量：" + Arrays.toString(vector));
    // 输出示例：[2, 2, 1, 2, 1]
    // 含义：'编'出现2次，'程'出现2次，'j'出现1次，'a'出现2次，'v'出现1次
*/
    private static int[] getCharFrequencyVector(String word, List<Character> uniqueChars) {
        int[] vector = new int[uniqueChars.size()];
        char[] chars = word.toCharArray();
        for (int i = 0; i < uniqueChars.size(); i++) {
            int count = 0;
            char target = uniqueChars.get(i);
            for (char c : chars) {
                if (c == target) {
                    count++;
                }
            }
            vector[i] = count;
        }
        return vector;
    }

    /**
     * 计算向量的模长
     */
    /*
    // 原始字符串
    String word = "编程编程java";
    // 提取所有唯一字符
    Set<Character> charSet = new HashSet<>();
        for (char c : word.toCharArray()) {
        charSet.add(c);
    }
    List<Character> uniqueChars = new ArrayList<>(charSet);
    // 此处uniqueChars可能为：['编', '程', 'j', 'a', 'v']（顺序不固定）

    // 生成字符频率向量
    int[] vector = getCharFrequencyVector(word, uniqueChars);

    // 输出结果
        System.out.println("字符频率向量：" + Arrays.toString(vector));
    // 输出示例：[2, 2, 1, 2, 1]
    // 含义：'编'出现2次，'程'出现2次，'j'出现1次，'a'出现2次，'v'出现1次
*/
    private static double calculateNorm(int[] vector) {
        double sum = 0.0;
        for (int value : vector) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }
}
