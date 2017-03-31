#Oscar Miniet
	.data

#ArrayA: .space 72 #declares and array with 72 bytes (4 for each interger)
ArrayA: .word 89, 19, 91, -5, 23, -67, 31, 46, -71, -14, -10, 3, 67, 17, 11, -18, 43, -73 #loads array with the predetermined values
ArrayB: .space 44 #11 integer array 11 x 4 = 44
ArrayC: .space 28 #7 integer array 7 x 4 = 28 

indString: .asciiz "Index of the smallest positive number: "
Space: .asciiz " "
BString: .asciiz "\nArray B: "
CString: .asciiz "\nArray C: "

min: .word 100# will hold the smallest positive interger while running through loop
index: .word 0# will hold index of most current positive index

	.text
	.globl main
main:
POS:#Search for positive integers loop
	beq $t1, 72, ENDPOS#branches to outside of the loop if the index is equal to the size of the array
	lw $t0, ArrayA($t1)#loads first index of ArrayA into register $t0
	bgez $t0, ISPOSITIVE#branches if the value in  $t0 is greater than 0
	addi $t1, $t1, 4#if not then increments$t1 by 4 and... 
	j POS# jumps to top of loop


ISPOSITIVE:#Compare to find smallest positive integer
	lw $t4, min#loads current min into register $t4
	bge $t0, $t4, NOTMIN#branches to NOTMIN if the number is positive but greater than the current min 
	sw $t0, min#else stores the current value in $t0 into the min variable
	sw $t1, index#Also stores the current index
	addi $t1, $t1, 4#increments the value of the array index to be checked
	j POS#jumps back to top of loop

NOTMIN:
	addi $t1, $t1, 4#add 4 to value in $t1
	j POS#jumps back to top of loop 

ENDPOS: #End of loop checking for lowest possible positive number 
	sw $t4, min#saves the final value of $t0 into min (found in .data)
	lw $t5, index#loads the final value of index into $t5
	div $t5, $t5, 4#divides $t5 by 4
	sw $t5, index#saves divided $t5 value back into index to be printed later


PRINTINDEX: #set of instructions to print the index
	li $v0, 4#loads syscall instruction into $v0
	la $a0, indString#loads indstring into $a0 
	syscall#executes the print instruction

	li $v0, 1#same method
	lw $a0, index
	syscall


	li $t1, 0 #resetsthe counting index back to 0 ArrayA
	
TRANSFER: #start of loop to transfer the numbers from A to B and C
	beq $t1, 72, ENDTRANSFER#Exits loop when Array A has been completely run through
	lw $t0, ArrayA($t1)#loads first index of ArrayA into register $t0
	blez $t0, NEG #branches to NEG if value currently in $t0 is less than 0
	
	sw $t0, ArrayB($t2)
	addi $t2, $t2, 4
	addi $t1, $t1, 4
	j TRANSFER
	
	NEG: #Transfer into other array
	sw $t0, ArrayC($t3)
	addi $t3, $t3, 4
	addi $t1, $t1, 4
	j TRANSFER

ENDTRANSFER:
	li $v0, 4
	la $a0, BString
	syscall
	
	li $t1, 0  #resets t1 to zero to count the array indices
	
PRINTB:#loop to print out indices of ArrayB
	bge $t1, 44, ENDPRINTB#if greater than or equal to 44 branch to end of loop, else continue loop 
	lw $t0, ArrayB($t1)#loads current index of Array B into register $t0 
	addi $t1, $t1, 4#adds 4 to $t1 to get to the next interger index
	
	li $v0, 1#loads interger print instruction into $v0 
	move $a0, $t0#moves the value that was taken from the array into $a0 
	syscall#syscall to do the printing
	
	li $v0, 4#loads instruction to print string 
	la $a0, Space#loads a blank space into $a0 
	syscall#prints the blank space 
	
	j PRINTB#returns to top of loop 

ENDPRINTB:#end of loop 

	li $v0, 4# these 3 lines print out the beginning of the statement for Array C 
	la $a0, CString
	syscall

	li $t1, 0  #resets t1 to zero to count the array indices
	
PRINTC:#exact same concept as PRINTB -> ENDPRINTB
	
	bge $t1, 28, ENDPRINTC
	lw $t0, ArrayC($t1)
	addi $t1, $t1, 4
	
	li $v0, 1
	move $a0, $t0
	syscall
	
	li $v0, 4
	la $a0, Space
	syscall
	
	j PRINTC

ENDPRINTC:

	li $v0, 10#instruction to end the program 
	syscall
