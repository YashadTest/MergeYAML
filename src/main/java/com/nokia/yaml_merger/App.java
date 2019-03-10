package com.nokia.yaml_merger;
import java.io.IOException;
import java.util.Scanner;

public class App {

	public static void main(String[] args) throws IOException {
		/*	Normal CLI*/
		System.out.println("Enter the file path:");
		Scanner input= new Scanner(System.in);
		Merger merge = new Merger();
		String filePath=input.nextLine();
		while(!(merge.execute(filePath)==200)) {
			System.out.println("Enter the file path:");
			filePath= input.nextLine();

		}
		input.close();



	}
}