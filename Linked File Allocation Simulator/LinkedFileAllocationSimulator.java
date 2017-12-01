/* 
 * Team Members: Kyle Glaws, Jamaal Chaney, Drew Marschke
 * 		
 * 		Description: This program simulates the linked file allocation algorithm.
 * It begins by initializing a disk drive, disk.txt. Each line in disk.txt acts as
 * a disk block. The disk can store up to 1000 total disk blocks, and each block
 * can contain a maximum of 64 characters. The program then loads formatted strings,
 * which will act as files, from input.txt and splits them up into substrings. The 
 * substrings are then loaded into the disk blocks. At the end of each disk block 
 * is a 3 character pointer to the next substring in the file. The program also
 * maintains a file-allocation table as well as a list of unallocated disk blocks.
 *  	The command line interface enables the user to interact with the file
 * system in several ways...
 * 
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class LinkedFileAllocationSimulator {
	
	static final int DISK_SIZE = 1000;
	
	// Each block can have at most 64 chars, with 3 at the end reserved for pointers.
	// Example: "Two roads diverged in a yellow wood,\nAnd sorry I could not t089" 
	static final int BLOCK_SIZE = 61; 
	
	/*
	 * The disk will have up to 1,000 blocks, addressed 000 - 999
	 * Each block will have up to 64 chars, with 3 chars at the end
	 * reserved for pointers.
	 */
	static File disk;
	
	// file-allocation table
	static HashMap<String,Integer> file_table;
	
	// keep a list of available blocks on disk
	static ArrayList<Integer> free_blocks;
	
	public static void main(String [] args) throws IOException, FileNotFoundException {
		
		/*
		 * =============================================
		 * Format disk
		 * =============================================
		 */ 
		// create disk
		disk = new File("disk.txt");
		
		// create file-allocation table
		file_table = new HashMap<String,Integer>();
		
		// create list of free blocks
		free_blocks = new ArrayList<Integer>();
			
		// initialize all blocks
		BufferedWriter writer = new BufferedWriter(new FileWriter(disk));
		for (int i = 0; i<DISK_SIZE-1;i++){
			writer.write("\n");
		}
		writer.close();
		
		// Initialize list of free blocks (add all blocks except 0)
		for (int i = 1; i < DISK_SIZE; i++){
			free_blocks.add(i);
		}
		
		/*
		 * =================================================
		 * Read input.txt, store each line into an ArrayList
		 * =================================================
		 */
		ArrayList<String> files = new ArrayList<String>();
		Scanner fileScan = null;
		fileScan = new Scanner(new File ("input.txt"));
		while(fileScan.hasNextLine()){
			file_table.put(fileScan.nextLine(), file_table.size());
			files.add(fileScan.nextLine());
		}
		fileScan.close();
		
		/*
		 * ===========================================
		 * Load files into disk.txt
		 * ===========================================
		 */
		int i;
		for (i=0;i<files.size();i++){
			if(!write_file(files.get(i))){break;}
		}
		System.out.println(i + " files loaded successfully.");
		
		System.out.println(get_block(0));
		
		/* TO DO:
		 * 		1. retrieve files by traversing disk.txt
		 * 		2. User interface:
		 * 			Welcome! Type 'help' for a list of commands.
		 * 			>> help
		 * 			Available commands:
		 * 				ls - display files on disk
		 * 				new - write new file disk
		 * 				cat - display file to console
		 * 				rm - remove specified file
		 * 				exit - exit session
		 * 				help - display this help message
		 * 			>> 
		 */
	}

	/*
	 * =================================================
	 * boolean write_file(File,String)
	 * Splits file into blocks and loads them onto disk.
	 * 1 param:
	 * 		String file - new data to be added to disk
	 * =================================================
	 */
	public static boolean write_file(String file)throws IOException{
		
		// determine file size
		int numOfBlocks = 0;
		int fileSize = file.length();
		while (fileSize>0){
			if (fileSize <= BLOCK_SIZE){
				numOfBlocks++;
				break;
			}
			fileSize -= BLOCK_SIZE;
			numOfBlocks++;
		}
		
		// check if file is too big
		if (fileSize>free_blocks.size()){
			System.out.println("Cannot load more files, low disk space.");
			System.out.println(free_blocks.size() + " blocks (about " + free_blocks.size()*BLOCK_SIZE+ " chars) remaining.");
			return false;
		}
		
		// locate available blocks
		int blocks [] = new int [numOfBlocks];
		for (int i=0;i<numOfBlocks;i++){
			int random = (int) (Math.random() * 999) + 1;
			while(!free_blocks.contains(random)){
				random = (int) (Math.random() * 999) + 1;
			}
			blocks[i] = random;
			free_blocks.remove(new Integer(random));
		}
		
		// add first file block to file table
		List<String> lines = Files.readAllLines(disk.toPath());
		lines.set(0, lines.get(0) + blocks[0] +" ");
		Files.write(disk.toPath(), lines);
		
		// load file blocks onto disk
		for (int i=0; i<blocks.length; i++){	
			if (i == blocks.length-1){
				// -1 is the EOF marker
				write_block(file, blocks[i], -1);
				break;
			}
			else{
				write_block(file.substring(0, BLOCK_SIZE), blocks[i], blocks[i+1]);
			}
			file = file.substring(BLOCK_SIZE);
		}
		return true;
	}
	
	/*
	 * ==============================================
	 * void write_to_disk(File,String,int,int)
	 * Writes file data to a given block.
	 * 4 params:
	 * 		String data - new data to be added
	 * 		int blockNumber - location in disk
	 * 		int pointer - pointer to the next piece
	 * 						of the file (this gets
	 * 						appended to 'data')
	 * ==============================================
	 */
	public static void write_block(String data, int blockNumber, int pointer)throws IOException{
		
		List<String> lines = Files.readAllLines(disk.toPath());
		if (pointer == -1){
			// ensure that EOF is exactly 3 chars long at the end of disk block
			lines.set(blockNumber-1, data + " -1");
		}
		else{
			lines.set(blockNumber-1, data + pointer);
		}
		Files.write(disk.toPath(), lines);
	}
}