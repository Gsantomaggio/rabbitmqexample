package com.test.rmq;

public interface Constants {
	public static final String RECIPE_NR  = "05/02"; 
	public static final String HEADER     = " ** RabbitmqCookBook - Recipe number " + RECIPE_NR + " **";
	public static final String exchange   = "search_exchange_" + RECIPE_NR;
	public static final String queue = "queue_consumer_search_" +RECIPE_NR;
}
