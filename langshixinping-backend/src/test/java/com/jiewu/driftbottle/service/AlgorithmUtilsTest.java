package com.jiewu.driftbottle.service;

import com.jiewu.driftbottle.utlis.AlgorithmUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 算法工具类测试
 */
public class AlgorithmUtilsTest {


    @Test
    void  testMinDistance() {
        String str1 = "我是狗";
        String str2 = "我不是狗";
        String str3 = "我是鱼不是狗";
//        String str4 = "我是猫";
        // 1
        int score1 = AlgorithmUtils.minDistance(str1, str2);
        // 3
        int score2 = AlgorithmUtils.minDistance(str1, str3);
//        System.out.println(score1);
//        System.out.println(score2);

        List<String> tags1 = Arrays.asList("java", "编程", "开发", "后端");
        List<String> tags2 = Arrays.asList("java", "开发", "后端", "spring");
        List<String> tags3 = Arrays.asList("java", "开发", "后端");
        List<String> tags4 = Collections.singletonList("java");

        // 1
        int score11 = AlgorithmUtils.minDistance(tags1, tags2);
        // 3
        int score12 = AlgorithmUtils.minDistance(tags1, tags3);
        // 2
        int score13 = AlgorithmUtils.minDistance(tags1, tags4);
        System.out.println(score11);
        System.out.println(score12);
        System.out.println(score13);
    }

    @Test
    void testCosineSimilarity() {
        // 1. 计算两组标签的余弦相似度
        List<String> tags1 = Arrays.asList("java", "编程", "开发", "后端");
        List<String> tags2 = Arrays.asList("java", "开发", "后端", "spring");
        List<String> tags3 = Arrays.asList("java", "开发", "后端");
        List<String> tags4 = Arrays.asList("Java", "开发", "前端");
        List<String> tags5 = Arrays.asList("Java", "开发", "后端");
        double tagSimilarity = AlgorithmUtils.cosineSimilarity(tags1, tags2);
        double tagSimilarity1 = AlgorithmUtils.cosineSimilarity(tags1, tags3);
        double tagSimilarity2 = AlgorithmUtils.cosineSimilarity(tags1, tags4);
        double tagSimilarity3 = AlgorithmUtils.cosineSimilarity(tags1, tags5);
        System.out.println("标签列表相似度: " + tagSimilarity );
        System.out.println("标签列表相似度1: " + tagSimilarity1 );
        System.out.println("标签列表相似度2: " + tagSimilarity2 );
        System.out.println("标签列表相似度3: " + tagSimilarity3 );

        // 输出示例: 标签列表相似度: 0.8660254037844386


        // 2. 计算两个字符串的余弦相似度
        String str1 = "人工智能与机器学习";
        String str2 = "机器学习与人工智能";
        double stringSimilarity = AlgorithmUtils.cosineSimilarity(str1, str2);
        System.out.println("字符串相似度: " + stringSimilarity);
        // 输出示例: 字符串相似度: 1.0 (因为两个字符串包含完全相同的字符)


        // 3. 测试差异较大的情况
        String str3 = "苹果";
        String str4 = "香蕉";
        double diffSimilarity = AlgorithmUtils.cosineSimilarity(str3, str4);
        System.out.println("差异字符串相似度: " + diffSimilarity);
        // 输出示例: 差异字符串相似度: 0.0 (没有共同字符)
    }


}
