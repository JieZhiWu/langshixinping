package com.jiewu.driftbottle.service;

import com.google.gson.Gson;
import com.jiewu.driftbottle.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

/**
 * 用户插入单元测试，注意打包时要删掉或忽略，不然打一次包就插入一次
 */
@SpringBootTest
public class InsertUserTest {

    @Resource
    private UserService userService;

    // 线程池设置
    private final ExecutorService executorService = new ThreadPoolExecutor(
            16, 1000, 10, TimeUnit.SECONDS,
            new java.util.concurrent.ArrayBlockingQueue<>(10000),
            new ThreadPoolExecutor.CallerRunsPolicy() // 当队列满时，让提交任务的线程执行任务，避免任务丢失
    );

    /**
     * 循环插入用户  耗时：7260ms
     * 批量插入用户   1000  耗时： 4751ms
     */
    @Test
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("'jiewu'+i");
            user.setUserAccount("'jiewu'+i");
            user.setAvatarUrl("https://file.nbfox.com/wp-content/uploads/2022/08/12/1660312684-Jean-Baptiste-Camille_Corot_-_Orpheus_Leading_Eurydice_from_the_Underworld_-800px.jpg");
            user.setProfile("一个人");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("19245677890");
            user.setEmail("fengshier@example.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("i+100");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList, 100);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());

    }


    /**
     * 并发批量插入用户   100000  耗时： 26830ms
     */

    // 方向标签库
    private static final List<String> DIRECTION_TAGS = Arrays.asList(
            "诗歌", "散文", "小说", "哲学"
    );

    // 喜欢看标签库
    private static final List<String> LIKE_READ_TAGS = Arrays.asList(
            "中国文学", "外国文学", "现代诗歌", "中国诗词", "现实主义", "浪漫主义"
    );

    // 段位标签库
    private static final List<String> LEVEL_TAGS = Arrays.asList(
            "入门", "略知一二", "小有成就"
    );

    // 身份标签库
    private static final List<String> IDENTITY_TAGS = Arrays.asList(
            "小学", "初中", "高中", "大学", "学生", "待业", "已就业", "研究生"
    );

    // 状态标签库
    private static final List<String> STATUS_TAGS = Arrays.asList(
            "乐观", "低迷", "平淡", "单身", "已婚", "恋爱"
    );

    // 其他标签库
    private static final List<String> OTHER_TAGS = Arrays.asList(
            "男", "女"
    );

    // 预设个性签名库
    private static final List<String> PROFILES = Arrays.asList(
            "热爱文学的爱好者", "在文字世界中探索", "享受阅读的乐趣",
            "希望找到志同道合的朋友", "分享读书心得", "以文会友"
    );

    @Test
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 500000;
        int batchSize = 5000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < INSERT_NUM / batchSize; i++) {
            final int batchIndex = i;
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<User> userList = new ArrayList<>(batchSize);
                for (int j = 0; j < batchSize; j++) {
                    int sequence = batchIndex * batchSize + j;
                    User user = new User();

                    // 生成用户名和账号（确保唯一性）
                    user.setUsername("user_" + sequence);
                    user.setUserAccount("account_" + sequence);

                    // 随机选择头像（可以扩展更多图片链接）
                    String[] avatarUrls = {
                            "https://file.nbfox.com/wp-content/uploads/2021/11/17/1637129003-2021-11-16_231242-tuya.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/04/06/cri_000000386470.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/04/18/O5ftB6.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2022/03/16/1647418871-Small-worlds-IV-600px-1.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/03/Six_Sunflowers_1888.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/03/12/20200312112135-5e6a1b3f460d0.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2022/03/08/1646730510-large-34a14dc6721ec882e7de859fa788474bba69b5ee.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/04/10/20200410030810-5e8f729a04ae3.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/04/10/20200410091247-5e8fc80f0502c.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/03/11/20200311190149-5e69359d4d3e5.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/03/11/20200311181548-5e692ad4c84d3.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2021/10/16/1634366159-20211016143559-616a72cf3619d.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/03/20/1024px-John_Everett_Millais_-_Ophelia_-_Google_Art_Project.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2021/09/12/1631435205-Lady-Godiva-1898John-Collier-750px.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/04/16/20200416024418-5e97c68272f61.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/03/14/20200314110609-5e6cbaa171e81.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/04/Portrait-of-Paris-von-Gutersloh.jpg",

                    };
                    user.setAvatarUrl(avatarUrls[random.nextInt(avatarUrls.length)]);

                    // 随机选择个性签名
                    user.setProfile(PROFILES.get(random.nextInt(PROFILES.size())));

                    // 随机性别（0-未知，1-男，2-女），同时设置其他标签中的性别标签
                    int gender = random.nextInt(3);
                    user.setGender(gender);
                    Set<String> otherTags = new HashSet<>();
                    if (gender == 1) {
                        otherTags.add("男");
                    } else if (gender == 2) {
                        otherTags.add("女");
                    }

                    // 生成随机手机号（模拟）
                    user.setPhone("1" + (1000000000 + random.nextInt(900000000)));

                    // 生成随机邮箱
                    user.setEmail("user" + sequence + "@example.com");

                    user.setUserStatus(0); // 正常状态
                    user.setUserRole(0);   // 普通用户
                    user.setPlanetCode(String.valueOf(10000 + sequence));
                    user.setUserPassword("12345678");

                    // 生成方向标签（1个）
                    Set<String> directionTags = new HashSet<>();
                    directionTags.add(DIRECTION_TAGS.get(random.nextInt(DIRECTION_TAGS.size())));

                    // 生成喜欢看标签（1-3个）
                    Set<String> likeReadTags = new HashSet<>();
                    int likeReadTagCount = 1 + random.nextInt(3);
                    for (int k = 0; k < likeReadTagCount; k++) {
                        likeReadTags.add(LIKE_READ_TAGS.get(random.nextInt(LIKE_READ_TAGS.size())));
                    }

                    // 生成段位标签（1个）
                    Set<String> levelTags = new HashSet<>();
                    levelTags.add(LEVEL_TAGS.get(random.nextInt(LEVEL_TAGS.size())));

                    // 生成身份标签（1个）
                    Set<String> identityTags = new HashSet<>();
                    identityTags.add(IDENTITY_TAGS.get(random.nextInt(IDENTITY_TAGS.size())));

                    // 生成状态标签（1个）
                    Set<String> statusTags = new HashSet<>();
                    statusTags.add(STATUS_TAGS.get(random.nextInt(STATUS_TAGS.size())));

                    // 合并所有标签
                    Set<String> allTags = new HashSet<>();
                    allTags.addAll(directionTags);
                    allTags.addAll(likeReadTags);
                    allTags.addAll(levelTags);
                    allTags.addAll(identityTags);
                    allTags.addAll(statusTags);
                    allTags.addAll(otherTags);

                    // 转换为JSON格式字符串
                    user.setTags(new Gson().toJson(new ArrayList<>(allTags)));

                    userList.add(user);
                }
                userService.saveBatch(userList, batchSize);
                System.out.println("ThreadName：" + Thread.currentThread().getName() + "，完成第" + (batchIndex + 1) + "批插入");
            }, executorService);
            futureList.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        executorService.shutdown(); // 关闭线程池

        stopWatch.stop();
        System.out.println("总耗时：" + stopWatch.getLastTaskTimeMillis() + "ms");
    }
}