package com.atguigu;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

//实验一，存在超卖情况
public class SecKill_redis1 {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecKill_redis1.class);

	public static void main(String[] args) {

		Jedis jedis = new Jedis("192.168.1.102", 6379);

		System.out.println(jedis.ping());

		jedis.close();

	}

	public static boolean doSecKill(String uid, String prodid) throws IOException {

		//1.准备存储的key
		//拼key
		String qtkey = "sk:"+prodid+":qt";
		String usrkey = "sk:"+prodid+":usr";
		
		//System.out.println("qtkey:"+qtkey);
		//System.out.println("usrkey:"+usrkey);
		
		//2.判断用户是否已经秒到，不能重复秒
		Jedis jedis = new Jedis("192.168.137.110",6379);
		if(jedis.sismember(usrkey, uid)) {			
			System.out.println("不能重复秒杀");
			jedis.close();
			return false ;
		}
		
		//3.判断库存
		String qtkeystr = jedis.get(qtkey);
		if(qtkeystr==null || "".equals(qtkeystr.trim())) {
			System.out.println("未初始化库存");
			jedis.close();
			return false ;
		}
		
		int qt = Integer.parseInt(qtkeystr);
		if(qt<=0) {
			System.out.println("已经秒光");
			jedis.close();
			return false;
		}
		
		//4.减少库存
		jedis.decr(qtkey);
		
		//5.加人
		jedis.sadd(usrkey, uid);		
		jedis.close();
		System.out.println("秒杀成功");
		
		return true;
	}

}
