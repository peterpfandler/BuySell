

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.junit.Test;

public class TestInstruction {

	@Test
	public void testInstruction() {
		Instruction i = new Instruction();
		//check if the generated instruction has a valid entity
		assertTrue(i.entities.contains(i.getEntity()));
	}

	@Test
	public void testSetCurrency() {
		Instruction i = new Instruction();
		//check if the currency can be set to an invalid value
		assertFalse(i.setCurrency("123"));
		//check if the currency can be set to a valid value
		assertTrue(i.setCurrency("AED"));
	}

	@Test
	public void testInstructionStringStringFloatStringDateDateLongFloat() {
		Instruction i = new Instruction("ATN", "B", 0.2F, "BRL", new Date(), new Date(), 19, 50);
		Instruction j = new Instruction("ATN", "B", 0.2F, "BRL", new Date(), new Date(), 19, 50);
		//The object ID is incremented every time a new Instruction is generated
		//check if the ID of the newer instruction is bigger 
		assertTrue(new Long(i.getId()).compareTo(new Long(j.getId())) < 0);
	}

	@Test
	public void testPrintTransactionsHeader() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Instruction.printTransactionsHeader(outputStream);
		//check if the transaction header contains some of the column titles
		assertTrue(outputStream.toString().contains("agreedFX"));
		assertTrue(outputStream.toString().contains("Instr. Date"));
		assertTrue(outputStream.toString().contains("Settl. Date"));
		assertTrue(outputStream.toString().contains("USD amount"));
	}

	@Test
	public void testPrint() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Instruction k = new Instruction();
		k.print(outputStream);
		//check if the instruction output contains some of its fields
		assertTrue(outputStream.toString().contains(k.getEntity()));
		assertTrue(outputStream.toString().contains(String.format("%10.2f|", k.getValue())));
	}

	@Test
	public void testIsIncoming() {
		Instruction l = new Instruction("ATN", "B", 0.2F, "BRL", new Date(), new Date(), 19, 50);
		//check if the instruction type "B" is outgoing
		assertFalse(l.isIncoming());
		l = new Instruction("ATN", "S", 0.2F, "BRL", new Date(), new Date(), 19, 50);
		//check if the instruction type "S" is incoming
		assertTrue(l.isIncoming());
	}

	@Test
	public void testGetId() {
		Instruction m = new Instruction("ATN", "B", 0.2F, "BRL", new Date(), new Date(), 19, 50);
		//check if the instruction id is not null
		assertTrue(m.getId()>0);
	}

	@Test
	public void testGetDate() {
		Date testDate = new Date();
		Instruction n = new Instruction("ATN", "B", 0.2F, "BRL", testDate, new Date(), 19, 50);
		//check if the instruction date equals to the given date
		assertTrue(n.getDate().equals(testDate));
	}

	@Test
	public void testGetEntity() {
		Instruction o = new Instruction("ATN", "B", 0.2F, "BRL", new Date(), new Date(), 19, 50);
		//check if the instruction entity equals to the given entity
		assertTrue(o.getEntity().equals("ATN"));
	}

	@Test
	public void testGetValue() {
		Instruction p = new Instruction("ATN", "B", 0.2F, "BRL", new Date(), new Date(), 19, 50);
		//check if the instruction value calculation is correct
		assertTrue(new Float(p.getValue()).equals(0.2F*19*50));
	}

}
