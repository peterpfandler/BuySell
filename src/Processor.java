import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Processor {

	static List<Instruction> listIncoming = new ArrayList<Instruction>();
	static List<Instruction> listOutgoing = new ArrayList<Instruction>();
	static Map <String,Float> sortedIncomingInstructionsByEntity;
	static Map <Date,Float> sortedIncomingInstructionsByDate;
	static Map <String,Float> sortedOutgoingInstructionsByEntity;
	static Map <Date,Float> sortedOutgoingInstructionsByDate;
	static Instruction instruction;
	
	public static void main(String[] args) {
		//generate 30 random transactions
		generateTransactions();
		//print all transactions, incoming and outgoing separately
		printIncomingInstructions();
		printOutgoingInstructions();
		
		printSeparator();
		printSeparator();
		//now comes the solution: 
		//this is the report that shows amount in USD settled incoming everyday
		printRankedIncomingInstructionsByDate();
		//this is the report that shows amount in USD settled outgoing everyday
		printRankedOutgoingInstructionsByDate();
		//these are the reports that rank the entities based on incoming and outgoing amount
		printRankedIncomingInstructionsByEntity();
		printRankedOutgoingInstructionsByEntity();
	}

	private static void collectInstruction(Instruction instruction) {
		// Collect the generated instructions in an incoming or an outgoing pool.
		if (instruction.isIncoming()) {
			listIncoming.add(instruction);
		} else {
			listOutgoing.add(instruction);
		}
	}

	private static void generateTransactions() {
		/*
		 * Generate enough data to have a satisfactory amount of incoming and outgoing transactions.
		 * The middle loop generates the special currencies, so that the settlement dates can be visually checked for both types of workday constraints.
		 * By default the instruction dates are 2 days before the settlement dates. 
		 * If the visual check doesn't show any settlement date adjustments, then run the program again.
		 */
		System.out.println("Generating transactions.");
		printSeparator();
		Instruction.printTransactionsHeader(System.out);
		for (int i=0; i<7; i++) {
			instruction = new Instruction();
			collectInstruction(instruction);
			instruction.print(System.out);
		}
		for (int i=0; i<7; i++) {
			instruction = new Instruction();
			instruction.setCurrency("AED");
			collectInstruction(instruction);
			instruction.print(System.out);
			instruction = new Instruction();
			instruction.setCurrency("SAR");
			collectInstruction(instruction);
			instruction.print(System.out);
		}
		for (int i=0; i<9; i++) {
			instruction = new Instruction();
			collectInstruction(instruction);
			instruction.print(System.out);
		}
		printSeparator();
	}
	
	private static void printIncomingInstructions() {
		System.out.println("All Incoming Transactions");
		printSeparator();
		Instruction.printTransactionsHeader(System.out);
		listIncoming.sort(Instruction.InstructionValueComparator);
		listIncoming.iterator().forEachRemaining(x-> {x.print(System.out);});
		printSeparator();
	}

	private static void printRankedIncomingInstructionsByEntity() {
		System.out.println("Entity Rank by Incoming Amount");
		printSeparator();
		HashMap<String,Float> rankIncoming = new HashMap<String,Float>();
		for (Instruction i: listIncoming) {
			aggregateInstructionsByEntity(i, rankIncoming);
		}
		sortedIncomingInstructionsByEntity = sortInstructions(rankIncoming);
        printEntityMap(sortedIncomingInstructionsByEntity);
		printSeparator();
	}

	private static void printRankedIncomingInstructionsByDate() {
		System.out.println("Amount in USD settled incoming everyday");
		printSeparator();
		HashMap<Date,Float> rankIncoming = new HashMap<Date,Float>();
		for (Instruction i: listIncoming) {
			aggregateInstructionsByDate(i, rankIncoming);
		}
		sortedIncomingInstructionsByDate = sortInstructions(rankIncoming);
        printDateMap(sortedIncomingInstructionsByDate);
		printSeparator();
	}

	private static void aggregateInstructionsByEntity(Instruction i, HashMap<String, Float> rankMap) {
		String key = i.getEntity();
		if (rankMap.containsKey(key)) {
			float newValue = i.getValue()+rankMap.get(key);
			rankMap.replace(key, new Float(newValue));
		} else {
			rankMap.put(key, i.getValue());
		}
	}
	
	private static void aggregateInstructionsByDate(Instruction i, HashMap<Date, Float> rankMap) {
		Date key = i.getDate();
		if (rankMap.containsKey(key)) {
			float newValue = i.getValue()+rankMap.get(key);
			rankMap.replace(key, new Float(newValue));
		} else {
			rankMap.put(key, i.getValue());
		}
	}
	
	private static void printOutgoingInstructions() {
		System.out.println("All Outgoing Transactions");
		printSeparator();
		Instruction.printTransactionsHeader(System.out);
		listOutgoing.sort(Instruction.InstructionValueComparator);
		listOutgoing.iterator().forEachRemaining(x-> {x.print(System.out);});
		printSeparator();
	}
	
	private static void printRankedOutgoingInstructionsByEntity() {
		System.out.println("Entity Rank by Outgoing Amount");
		printSeparator();
		HashMap<String,Float> rankOutgoing = new HashMap<String,Float>();
		for (Instruction i: listOutgoing) {
			aggregateInstructionsByEntity(i, rankOutgoing);
		}
		sortedOutgoingInstructionsByEntity = sortInstructions(rankOutgoing);
        printEntityMap(sortedOutgoingInstructionsByEntity);
		printSeparator();
	}

	private static void printRankedOutgoingInstructionsByDate() {
		System.out.println("Amount in USD settled outgoing everyday");
		printSeparator();
		HashMap<Date,Float> rankOutgoing = new HashMap<Date,Float>();
		for (Instruction i: listOutgoing) {
			aggregateInstructionsByDate(i, rankOutgoing);
		}
		sortedOutgoingInstructionsByDate = sortInstructions(rankOutgoing);
        printDateMap(sortedOutgoingInstructionsByDate);
		printSeparator();
	}

	
	private static void printSeparator() {
		System.out.print(String.format("******************"));
		System.out.print(String.format("******************"));
		System.out.print(String.format("******************"));
		System.out.print(String.format("******************"));
		System.out.println(String.format("****************"));
	}

    private static <K, V> Map<K, V> sortInstructions(Map<K, V> instructions) {

    	/* Sort the instructions based on requirements:
    	 * If the Map has a String type of key, then the key is an entity name.
    	 * Otherwise the key is a date.
    	 * The values are always amounts.  
    	 */
    	
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(instructions.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1,
                               Map.Entry<K, V> o2) {
            	int retval = 0;
            	if (o1.getKey() instanceof Date) {
            		//dates are sorted in ascending order
                    retval = (((Date)o1.getKey()).compareTo((Date)o2.getKey()));            		
            	} else if (o1.getKey() instanceof String) {
            		//amounts are sorted in descending order
                    retval = (int)(((Float)o2.getValue()).compareTo((Float)o1.getValue()));
            	}
            	return retval;
            }
        });

        Map<K, V> sortedMap = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static <K, V> void printEntityMap(Map<K, V> map) {
    	/*
    	 * Print the sorted list of transactions.
    	 * The Map contains the aggregated settled amounts by entities. The key is the entity name, the value is the amount.
    	 */
    	System.out.println(String.format("%4s|", "Ent") + String.format("%10s|", "USD Amount"));
        for (Map.Entry<K, V> entry : map.entrySet()) {
        	if (map.equals(sortedIncomingInstructionsByEntity)) {
                System.out.println(String.format("%4s|", (String)(entry.getKey())) + String.format("%10.2f|", entry.getValue()));
        	} else if (map.equals(sortedOutgoingInstructionsByEntity)) {
                System.out.println(String.format("%4s|", (String)(entry.getKey())) + String.format("%10.2f|", entry.getValue()));
        	}
        }
    }
    
    public static <K, V> void printDateMap(Map<K, V> map) {
    	/*
    	 * Print the sorted list of transactions.
    	 * The Map contains the aggregated settled amounts by date. The key is the date, the value is the amount.
    	 */
    	System.out.println(" Instr. Date |" + String.format("%10s|", "USD Amount"));
        for (Map.Entry<K, V> entry : map.entrySet()) {
        	if (map.equals(sortedIncomingInstructionsByDate)) {
                System.out.println(Instruction.simpleDateFormat.format((entry.getKey())) + String.format("%10.2f|", entry.getValue()));
        	} else if (map.equals(sortedOutgoingInstructionsByDate)) {
                System.out.println(Instruction.simpleDateFormat.format((entry.getKey())) + String.format("%10.2f|", entry.getValue()));
        	}
        }
    }
    	
}
