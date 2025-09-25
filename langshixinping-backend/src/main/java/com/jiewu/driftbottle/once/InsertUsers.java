package com.jiewu.driftbottle.once;

import com.google.gson.Gson;
import com.jiewu.driftbottle.model.domain.User;
import com.jiewu.driftbottle.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class InsertUsers {

    @Resource
    private UserService userService;

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

    // 线程池设置
    private final ExecutorService executorService = new ThreadPoolExecutor(
            16, 1000, 10, TimeUnit.SECONDS,
            new java.util.concurrent.ArrayBlockingQueue<>(10000),
            new ThreadPoolExecutor.CallerRunsPolicy() // 当队列满时，让提交任务的线程执行任务，避免任务丢失
    );

//    开启定时任务后, 每次程序运行后5秒 执行该方法 (插入十万随机数据)
//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
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
                            "https://file.nbfox.com/wp-content/uploads/2020/03/12/20200312135038-5e6a3e2e72118.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2021/10/16/1634365699-20211016142819-616a71032c352.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/03/12/20200312135039-5e6a3e2f061e9.jpg",
                            "https://file.nbfox.com/wp-content/uploads/2020/03/12/20200312135030-5e6a3e268c523.jpg"
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
        System.out.println("总耗时：" + stopWatch.getLastTaskTimeMillis()+ "ms");
    }
}