package com.jiewu.driftbottle.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiewu.driftbottle.model.domain.User;
import com.jiewu.driftbottle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: shayu
 * @date: 2022/12/11
 * @ClassName: yupao-backend01
 * @Description:        数据预热
 */

@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 重点用户
    private final List<Long> mainUserList = Arrays.asList(1L,2L);

    // 每天执行，预热推荐用户 (每天3.55)
    @Scheduled(cron = "0 55 3 * * ?")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock("langshi:lock:precachejob:doCacheRecommendUser");
        try {
            if (lock.tryLock(0, 30000L, TimeUnit.MILLISECONDS)) {
                //查数据库
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                Page<User> userPage = userService.page(new Page<>(1,20),queryWrapper);
                String redisKey = String.format("langshi:user:recommend:%s",mainUserList);
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                //写缓存,30s过期
                try {
                    valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
                } catch (Exception e){
                    log.error("redis set key error",e);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error",e);
        } finally {
            //只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

}