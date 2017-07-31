package com.test.rmq;

import java.util.HashMap;
import java.util.Map;

public class Book {
	private int bookKey;
	private String bookDescription;
	private String author;
	
	public void setBookKey(int value) 
	{
		bookKey = value;
	}
	
	public int getBookKey()
	{
	  return bookKey;	
	}
	
	public void setBookDescription(String value)
	{
		bookDescription = value;
	}
	
	public String getBookDescription()
	{
		return bookDescription;
	}
	
	public void setAuthor(String value)
	{
		author = value;
	}
	
	public String getAuthor()
	{
		return author ;
	}
	
	
	public static Map<Integer,Book> buildSimpleBookList(){
		Map<Integer,Book> books = new HashMap<Integer, Book>();
		for (int i = 1; i < 11; i++) {
			Book book = new Book();
			book.setBookKey(i);
			book.setBookDescription("History VOL: " + i);
			book.setAuthor("John Doe");
			books.put(i,book);
		}
		return books;		
	}			
	
	
}
