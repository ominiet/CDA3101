import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class MIPSsim {
	public static ArrayList<Line> instructions;
	public static ArrayList<Line> memory;

	public static void main(String[] args){

		String fileName = args[0]; 	//necessary for receiving the file name 
		Disassemble(fileName); 		//takes the file name, reads and creates a disassembly.txt file
		simulate();					//simulates the code and creates the simulation.txt file	
	}
	public static void Disassemble(String fileName){	//for reading/disassembly.txt file
		instructions = new ArrayList<Line>();
		memory = new ArrayList<Line>();
		boolean unfound = true;
		String inst = "";
		
		while (unfound == true){
			try{
				File outfile = new File("disassembly.txt");
				if (!outfile.exists()){ 
					outfile.createNewFile();
				}
				PrintWriter writer = new PrintWriter(outfile);
				File file = new File(fileName);
				Scanner reader = new Scanner(file);
				unfound = false;
				boolean breakCheck = false;
				Line line = new Line();
				while (reader.hasNextLine()){
					inst = reader.nextLine();
					if (breakCheck == false){	//separates instructions into 3 categories
						if (inst.substring(0, 3).compareTo("010") == 0){//checks if the first 3 digits match
							line = new Cat2(inst);	//creates an object out of that instruction type
							instructions.add(line);
						}
						else if (inst.substring(0, 3).compareTo("100") == 0){
							line = new Cat3(inst);
							instructions.add(line);
						}
						else if (inst.substring(0, 3).compareTo("001") == 0){
							line = new Cat1(inst);
							instructions.add(line);
							if (((Cat1) line).string.substring(3,6).compareTo("111") == 0){	//stops creation of 3 category objects
								breakCheck = true;											//if a break happens
								instructions.add(line);
							}
						}
					}
					else{
						line = new mem(inst);	//turns remaining lines into memory lines
						memory.add(line);		
					}
					writer.println(line.toString());//prints to disassembly.txt
				}
				writer.close();
			}catch (Exception e){
				e.printStackTrace();
			}
			
		}
	}
	public static void simulate(){// not done commenting
		int[] registers = new int[32];	//holds register values
		int cycle = 0;					//holds cycle number
		int i = 0;						//holds index of instruction
		int PC = 0;					//holds actual PC value
		int memStart = 60 + instructions.size()*4;//starting value of the memory PC
		boolean end = false;//for knowing when the loop should finish
		Line instruction;
		
		try{
			File simfile = new File("simulation.txt");//creates simulation.txt if needed
			if (!simfile.exists()){ 
				simfile.createNewFile();
			}
			PrintWriter simwriter = new PrintWriter(simfile);

			while (end == false){/// start of simulation loop
				cycle++;
				PC = 64 + 4*i;
				instruction = instructions.get(i);
				
				if (instruction.getCat().equals("001")){ //category1
					int rs= ((Cat1) instruction).getrs();//register work for Cat1
					int rt= ((Cat1) instruction).getrt();
					int offset = ((Cat1) instruction).getoffset();
					
					if(((Cat1) instruction).getOp().equals("NOP")){
					}
					else if(((Cat1) instruction).getOp().equals("J")){
						i = ((offset-64)/4)-1;
					}
					else if(((Cat1) instruction).getOp().equals("BEQ")){
						if (registers[rs] == registers[rt] ){
							i += offset;
						}
					}
					else if(((Cat1) instruction).getOp().equals("BNE")){
						if (registers[rs] != registers[rt] ){
							i += offset;
						}
					}
					else if(((Cat1) instruction).getOp().equals("BGTZ")){
						if (registers[rs] > 0){
							i += offset;
						}
					}
					else if(((Cat1) instruction).getOp().equals("SW")){
						int index = ((registers[rt]+offset)-memStart)/4;
						((mem) memory.get(index)).setVal(registers[rs]);
					}
					else if(((Cat1) instruction).getOp().equals("LW")){
						int index = ((registers[rt]+offset)-memStart)/4;
						registers[rs] = ((mem) memory.get(index)).getVal();

					}
					else if(((Cat1) instruction).getOp().equals("BREAK")){
						end = true;

					}
				}

				else if (instruction.getCat().equals("010")){ //category 2
					int dest = ((Cat2) instruction).getDest();//register work for Cat2
					int rs = ((Cat2) instruction).getrs();
					int rt = ((Cat2) instruction).getrt();
					
					if(((Cat2) instruction).getOp().equals("XOR")){
						registers[dest] = registers[rs] ^ registers[rt];
					}
					else if(((Cat2) instruction).getOp().equals("MUL")){
						registers[dest] = registers[rs] * registers[rt];
					}
					else if(((Cat2) instruction).getOp().equals("ADD")){
						registers[dest] = registers[rs] + registers[rt];
					}
					else if(((Cat2) instruction).getOp().equals("SUB")){
						registers[dest] = registers[rs] - registers[rt];
					}
					else if(((Cat2) instruction).getOp().equals("AND")){
						registers[dest] = registers[rs] & registers[rt];
					}
					else if(((Cat2) instruction).getOp().equals("OR")){
						registers[dest] = registers[rs] | registers[rt];
					}
					else if(((Cat2) instruction).getOp().equals("ADDU")){ 
						registers[dest] = registers[rs] + registers[rt];
					}
					else if(((Cat2) instruction).getOp().equals("SUBU")){ 
						registers[dest] = registers[rs] - registers[rt];
					}
				}
				else if (instruction.getCat().equals("100")){ //category 3
					int dest = ((Cat3) instruction).getDest();//register work for Cat3
					int rs = ((Cat3) instruction).getrs();
					int imm = ((Cat3) instruction).getimm();
					if(((Cat3) instruction).getOp().equals("ORI")){
						registers[dest] = registers[rs] | imm;
					}
					else if(((Cat3) instruction).getOp().equals("XORI")){
						registers[dest] = registers[rs] ^ imm;
					}
					else if(((Cat3) instruction).getOp().equals("ADDI")){
						registers[dest] = registers[rs] + imm;
					}
					else if(((Cat3) instruction).getOp().equals("SUBI")){
						registers[dest] = registers[rs] - imm;
					}
					else if(((Cat3) instruction).getOp().equals("SRL")){
						registers[dest] = registers[rs] >> imm;
					}
					else if(((Cat3) instruction).getOp().equals("SRA")){
						registers[dest] = registers[rs] >> imm;
					}
					else if(((Cat3) instruction).getOp().equals("SLL")){
						registers[dest] = registers[rs] << imm;
					}
				}

				simwriter.println("--------------------");	//start of print sequence
				simwriter.println("Cycle " + cycle + ":\t" + PC + "\t" + instruction.getP());
				simwriter.print("\nRegisters");
				int x = 0;
				int y = 0;
				int z = 0;
				while (x < 4){
					y=0;
					simwriter.print("\nR" );
					if(z<10) simwriter.print("0");
					simwriter.print(z + ":");
					while (y < 8){
						simwriter.print("\t" + registers[z]);
						y++;
						z++;
					}

					x++;
				}
				while (x < 4){
					y=0;
					simwriter.print("\nR" );
					if(z<10) simwriter.print("0");
					simwriter.print(z + ":");
					while (y < 8){
						simwriter.print("\t" + registers[z]);
						y++;
						z++;
					}
					x++;
				}
				x=0;
				y=0;
				z=0;
				simwriter.print("\n\nData");
				while (x < memory.size()/8){
					y=0;
					simwriter.print("\n"+ memory.get(z).getLoc() + ":");
					while (y< 8){
						simwriter.print("\t" + ((mem)memory.get(z)).getVal());
						y++;
						z++;
					}
					x++;
				}
				simwriter.println("");
				i++;	
			}
			simwriter.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}

//each line will be represented as an object
class Line{//parent class
	String string = "";
	String cat = "";
	int loc;
	String pstring = "";
	static int count = 64;

	Line(String inst){
		this.string = inst;
		this.loc = count;
		this.cat = string.substring(0, 3);
		count = count + 4;
	}
	Line(){
	}
	public String toString() {
		String output;
		output = string + "\t" + loc;
		return output;
	}
	String getP(){
		return pstring;
	}
	int getLoc(){
		return loc;
	}
	String getCat(){
		return cat;
	}
	public int getTwos(String convert){//for converting to twos compliment when needed
		int twos = 0;
		if (convert.charAt(0) == '0'){//ignores if first digit is 0
			twos = Integer.parseInt(convert,2);//converts binary string to int regularly
		}
		else{
			int i = convert.lastIndexOf('1') ;	//finds last index of 1
			String temp = convert.substring(0,i).replace('1', '2');	//swaps all 0s for 1s and vice versa 
			temp = temp.substring(0,i).replace('0', '1');			//for all before that last 1
			temp = temp.replace('2','0');
			convert = temp + convert.substring(i, convert.length());
			twos = Integer.parseInt(convert,2);	//converts the new string to an int
			twos = twos * -1;					//makes int negative
		}
		return twos;
	}

}


class Cat1 extends Line{//subclass for Category 1
	String opcode = "";
	String operation = "";
	int rs;
	int rt;
	int	offset;

	Cat1(String inst) {
		super(inst);
		this.opcode = super.string.substring(3,6);
		if (opcode.compareTo("000") == 0){//individual ifs for each kind of Cat1
			this.operation = "NOP";
			this.pstring = "\t" + operation;
		}
		else if(opcode.compareTo("001") == 0){
			this.operation = "J";
			this.offset = Integer.parseInt(super.string.substring(6,32),2);
			offset *= 4;
			this.pstring = "\t" + operation + " " + "#" + offset;
		}
		else if(opcode.compareTo("010") == 0){
			this.operation = "BEQ";
			this.rs = Integer.parseInt(super.string.substring(6,11),2);
			this.rt = Integer.parseInt(super.string.substring(11,16),2);
			this.offset = getTwos(super.string.substring(16,32));
			this.pstring = "\t" + operation + " R" + rs + ", R" + rt + ", #" + offset;
		}
		else if(opcode.compareTo("011") == 0){
			this.operation = "BNE";
			this.rs = Integer.parseInt(super.string.substring(6,11),2);
			this.rt = Integer.parseInt(super.string.substring(11,16),2);
			this.offset = getTwos(super.string.substring(16,32));
			this.pstring = "\t" + operation + " R" + rs + ", R" + rt + ", #" + offset;
		}
		else if(opcode.compareTo("100") == 0){
			this.operation = "BGTZ";
			this.rs = Integer.parseInt(super.string.substring(6,11),2);
			this.offset = getTwos(super.string.substring(16,32));
			this.pstring = "\t" + operation + " R" + rs + ", #" + offset;

		}
		else if(opcode.compareTo("101") == 0){
			this.operation = "SW";
			this.rs = Integer.parseInt(super.string.substring(11,16),2);
			this.rt = Integer.parseInt(super.string.substring(6,11),2);
			this.offset = getTwos(super.string.substring(16,32));
			this.pstring = "\t" + operation + " R" + rs + ", " + offset + "(R" + rt + ")";
		}
		else if(opcode.compareTo("110") == 0){
			this.operation = "LW";
			this.rs = Integer.parseInt(super.string.substring(11,16),2);
			this.rt = Integer.parseInt(super.string.substring(6,11),2);
			this.offset = getTwos(super.string.substring(16,32));
			this.pstring = "\t" + operation + " R" + rs + ", " + offset + "(R" + rt + ")";
		}
		else if(opcode.compareTo("111") == 0){
			this.operation = "BREAK";
			this.pstring = "\t" + operation;
		}
	}
	String getOp(){
		return operation;
	}
	int getrs(){
		return rs;
	}
	int getrt(){
		return rt;
	}
	int getoffset(){
		return offset;
	}
	public String toString(){
		return super.toString() + pstring;
	}

}
class Cat2 extends Line{//subclass for Category 2
	String opcode = "";
	String operation = "";
	int dest;
	int rs;
	int rt;

	Cat2(String inst) {
		super(inst);
		this.opcode = super.string.substring(3,6);
		if (opcode.compareTo("000") == 0){//individual ifs for each kind of Cat2
			this.operation = "XOR";
		}
		else if(opcode.compareTo("001") == 0){
			this.operation = "MUL";
		}
		else if(opcode.compareTo("010") == 0){
			this.operation = "ADD";
		}
		else if(opcode.compareTo("011") == 0){
			this.operation = "SUB";
		}
		else if(opcode.compareTo("100") == 0){
			this.operation = "AND";
		}
		else if(opcode.compareTo("101") == 0){
			this.operation = "OR";
		}
		else if(opcode.compareTo("110") == 0){
			this.operation = "ADDU";
		}
		else if(opcode.compareTo("111") == 0){
			this.operation = "SUBU";
		}
		this.dest = Integer.parseInt(super.string.substring(6,11),2);
		this.rs = Integer.parseInt(super.string.substring(11,16),2);
		this.rt = Integer.parseInt(super.string.substring(16,21),2);
		this.pstring = "\t" + operation + " R" + dest + ", R" + rs + ", R" + rt;
	}
	public String toString(){ 
		return super.toString() + pstring;
	}
	String getOp(){
		return operation;
	}
	int getDest(){
		return dest;
	}
	int getrs(){
		return rs;
	}
	int getrt(){
		return rt;
	}

}
class Cat3 extends Line{//subclass for Category 3
	String opcode = "";
	String operation = "";
	int dest;
	int rs;
	int imm;

	Cat3(String inst) {
		super(inst);
		this.opcode = super.string.substring(3,6);
		if (opcode.compareTo("000") == 0){//(Individual ifs for each type of Cat3
			this.operation = "ORI";
			this.imm = Integer.parseInt(super.string.substring(16,32),2);
		}
		else if(opcode.compareTo("001") == 0){
			this.operation = "XORI";
			this.imm = Integer.parseInt(super.string.substring(16,32),2);
		}
		else if(opcode.compareTo("010") == 0){
			this.operation = "ADDI";
			this.imm = getTwos(super.string.substring(16,32));
		}
		else if(opcode.compareTo("011") == 0){
			this.operation = "SUBI";
			this.imm = getTwos(super.string.substring(16,32));
		}
		else if(opcode.compareTo("100") == 0){
			this.operation = "ANDI";
			this.imm = getTwos(super.string.substring(16,32));
		}
		else if(opcode.compareTo("101") == 0){
			this.operation = "SRL";
			this.imm = Integer.parseInt(super.string.substring(27,32),2);
		}
		else if(opcode.compareTo("110") == 0){
			this.operation = "SRA";
			this.imm = Integer.parseInt(super.string.substring(27,32),2);
		}
		else if(opcode.compareTo("111") == 0){
			this.operation = "SLL";
			this.imm = Integer.parseInt(super.string.substring(27,32),2);
		}
		this.dest = Integer.parseInt(super.string.substring(6,11),2);
		this.rs = Integer.parseInt(super.string.substring(11,16),2);
		this.pstring = "\t" + operation + " R" + dest + ", R" + rs + ", #" + imm;
	}
	public String toString(){
		return super.toString() + pstring;
	}
	String getOp(){
		return operation;
	}
	int getrs(){
		return rs;
	}
	int getimm(){
		return imm;
	}
	int getDest(){
		return dest;
	}
}
class mem extends Line{//subclass for Memory (last lines of code after BREAK)
	int val;
	mem(String inst) {
		super(inst);
		this.val = getTwos(super.string);
	}
	public String toString(){
		return super.toString() + "\t" + val;
	}
	int getVal(){
		return val;
	}
	void setVal(int value){
		this.val = value;
	}
}