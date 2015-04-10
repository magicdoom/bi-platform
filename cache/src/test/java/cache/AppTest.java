
package cache;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.codec.SerializationCodec;
import org.redisson.core.MessageListener;
import org.redisson.core.RQueue;
import org.redisson.core.RTopic;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

public class AppTest {
    
    @Test
    public void testRedisson() throws InterruptedException {
//        
//
//        Redisson redisson = Redisson.create(config);
//        
//        RTopic<String> topic = redisson.getTopic("topic");
//        
//        topic.addListener(new MessageListener<String>() {
//            
//            @Override
//            public void onMessage(String msg) {
//               System.out.println("get message:" + msg);
//                
//            }
//        });
//        
//        topic.publish("message publish");
//        
//        System.err.println("publish message");
//        
//        
//        
//        
//        
//        
//        RQueue<ApplicationEvent> queue = redisson.getQueue("queue");
////        queue.add(new ContextRefreshedEvent(null) );
////        queue.add(new ContextRefreshedEvent(null) );
//        
//        Thread t1 = new Thread(new GetFromQueue(queue));
//        
//        Thread t2 = new Thread(new GetFromQueue(queue));
//        
//        t1.start();
//        t2.start();
//        
//        Thread.sleep(30000);
//        
//        redisson.shutdown();
    }
    
    
//    private class GetFromQueue implements Runnable {
//
//        RQueue<ApplicationEvent> queue;
//        
//        /** 
//         * 构造函数
//         */
//        public GetFromQueue(RQueue<ApplicationEvent> queue) {
//            super();
//            this.queue = queue;
//        }
//
//        @Override
//        public void run() {
//            while(true) {
//                ApplicationEvent event = queue.poll();
//                if(event != null) {
//                    System.out.println("get event from queue by thread:" + Thread.currentThread() + " event:" + event);
//                    
//                }
//                
//            }
//        }
//        
//    }

    
    @Test
    public void testSentinelJedis() {
//        RedisSentinelConfiguration config = new RedisSentinelConfiguration();
//        
//        
//        JedisPoolConfig poolConfig = new JedisPoolConfig();
//
//        GenericObjectPoolConfig poolConfig1 = new GenericObjectPoolConfig();
//        poolConfig1.setMaxTotal(300);
//
//        Set<String> sentinels = new HashSet<>();
//        
//        
//        
//        JedisSentinelPool sentinelPool = new JedisSentinelPool(config.getMaster().getName(), sentinels, poolConfig1);
//        System.out.println("Current master: " + sentinelPool.getCurrentHostMaster().toString());
//        
//        JedisConnectionFactory factory = new JedisConnectionFactory(config);
//        factory.setPoolConfig(poolConfig);
//        
//        
//        Jedis jedis = sentinelPool.getResource();
//        System.out.println(jedis.getClient().getHost());
//        
////        jedis.auth("biplatform");
//        jedis.set("key", "value");
//        
//        System.out.println(jedis.get("key"));
//        sentinelPool.returnResource(jedis);
//        
//        Thread t = new Thread(new Runnable() {
//            
//            @Override
//            public void run() {
//                
//                System.out.println("start listen..");
////                System.out.println(jedis.get("key".getBytes()));
//                Jedis jedis1 = new Jedis(sentinelPool.getCurrentHostMaster().getHost(),sentinelPool.getCurrentHostMaster().getPort());
//                jedis1.auth("biplatform");
//                System.out.println("prepare jedis:" + jedis1);
//                System.out.println(jedis1.get("key"));
////                jedis1.subscribe(new PrintListener(), "channel");
////                jedis1.close();
//            }
//        });
//        
//        t.start();
        
//        Jedis jedis2 = sentinelPool.getResource();
//        
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            
//        }
//        System.out.println("start to publish..");
//        jedis2.publish("channel", "testMsg");
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            
//        }
        
//        System.out.println("publish end");
//        sentinelPool.returnResource(jedis2);
//        
//        
//        
//        
//        StringRedisTemplate template = new StringRedisTemplate(factory);
//        
//        template.execute(new RedisCallback<String>() {
//
//            @Override
//            public String doInRedis(RedisConnection connection) throws DataAccessException {
//                connection.set(template.getStringSerializer().serialize("key"), template.getStringSerializer().serialize("value"));
//                return null;
//                
//            }
//        });
//        
//        
//        
//        template.execute(new RedisCallback<String>() {
//
//            @Override
//            public String doInRedis(RedisConnection connection) throws DataAccessException {
//                byte[] value = connection.get(template.getStringSerializer().serialize("key"));
//                System.out.println(template.getStringSerializer().deserialize(value));
//                return null;
//                
//            }
//        });
    }
}

