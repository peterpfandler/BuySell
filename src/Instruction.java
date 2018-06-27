import java.util.Arrays;
import java.util.Comparator;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.text.SimpleDateFormat;

public class Instruction implements Comparable {
	private String entity;
	private String transactionType;
	private float agreedFX;
	private String currency;
	private Date instructionDate;
	private Date settlementDate;
	private long units;
	private float pricePerUnit;
	private long index;
	
	private Random rnd = new Random();
	private static long counter = 0;
	private static String datePattern=" dd MMM yyyy|";
	public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern); 
	List<String> entities = Arrays.asList("BUJ", "CSQ", "ATN", "TEH", "OFR", "WUC", "KXM", "FNT", "FGC", "TEQ");
	List<String> transactionTypes = Arrays.asList("B", "S");
	List<String> currencies = Arrays.asList("AED", "BRL", "CAD", "CHF", "CNY", "CZK", "HKD", "SAR", "SGD");
	
	public Instruction() {
		/*
		 * The randomization of dates is in the range of today +- 1 week, so having 30 transactions ensures to have more of them in a day.
		 * The exchange rates are randomized in the range of 0..1 (float value)
		 * Entities and currencies are selected randomly from a constant list.
		 * The units are a random value between 1 and 1000
		 * The price per unit varies between 200 and 300
		 * The Counter is the unique ID for the instruction.
		*/

		this.incrementCounter();
		String randomEntity = entities.get(rnd.nextInt(entities.size()));
		this.entity = randomEntity;
		String randomTransactionType = transactionTypes.get(rnd.nextInt(transactionTypes.size()));
		this.transactionType = randomTransactionType;
		this.agreedFX = rnd.nextFloat();
		String randomCurrency = currencies.get(rnd.nextInt(currencies.size()));
		this.currency = randomCurrency;
		long randomDate = generateRandomTransactionDate();
		this.instructionDate=new Date(randomDate);
		Date plannedSettlementDate = new Date(randomDate+48*3600000);
		if (isSpecialCurrency(this.currency)) { // workdays are Sun, Mon, Tue, Wed, Thu
			this.settlementDate = adjustSettlementDateForDifferentWorkDays(plannedSettlementDate);
		} else { //workdays are Mon, Tue, Wed, Thu, Fri
			this.settlementDate = adjustSettlementDate(plannedSettlementDate);
		}
		this.units = 1 + rnd.nextInt(999);
		this.pricePerUnit = 200 + rnd.nextInt(100);
	}
	
	public boolean setCurrency(String currency) {
		/*
		 * The currency is overwritten if it is in the set of redefined currencies.
		 * This function makes sure that there are records with special workdays.
		 */
		if (currencies.contains(currency)) {
			this.currency = currency;
			return true;
		}
		return false;
	}
	
	public Instruction(String entity, String transactionType, float agreedFX, String currency, Date instructionDate
			, Date settlementDate, long units, float pricePerUnit) {
	/*
	 * for later use
	 */
		this.incrementCounter();
		this.entity = entity;
		this.transactionType = String.format("%S", transactionType);
		this.agreedFX = agreedFX;
		this.currency = String.format("%S", currency);
		this.instructionDate = instructionDate;
		if (isSpecialCurrency(this.currency)) { // workdays are Sun, Mon, Tue, Wed, Thu
			this.settlementDate = adjustSettlementDateForDifferentWorkDays(settlementDate);
		} else { //workdays are Mon, Tue, Wed, Thu, Fri
			this.settlementDate = adjustSettlementDate(settlementDate);
		}
		this.units = units;
		this.pricePerUnit = pricePerUnit;
	}
	
	public static void printTransactionsHeader(OutputStream stream) {
		/*
		 * prints the header
		 */
		PrintWriter pw = new PrintWriter(stream);
		pw.write(String.format("%4s|", "Cnt"));
		pw.write(String.format("%4s|", "Ent"));
		pw.write(String.format("%5s|", "Type"));
		pw.write(String.format("%10s|", "agreedFX"));
		pw.write(String.format("%3s|", "CUR"));
		pw.write(String.format(" Instr. Date |"));
		pw.write(String.format(" Settl. Date |"));
		pw.write(String.format("%6s|", "Units"));
		pw.write(String.format("%10s|", "Unit price"));
		pw.write(String.format("%10s%n", "USD amount|"));
		pw.flush();
	}
	
	public void print(OutputStream stream) {
		/*
		 * prints the instruction record
		 */
		PrintWriter pw = new PrintWriter(stream);
		pw.write(String.format("%4s|", this.index));
		pw.write(String.format("%4s|", this.entity));
		pw.write(String.format("%5s|", this.transactionType));
		pw.write(String.format("%10.2f|", this.agreedFX));
		pw.write(String.format("%3s|", this.currency));
		pw.write(simpleDateFormat.format(this.instructionDate));
		pw.write(simpleDateFormat.format(this.settlementDate));
		pw.write(String.format("%6d|", this.units));
		pw.write(String.format("%10.2f|", this.pricePerUnit));
		pw.write(String.format("%10.2f|%n", this.pricePerUnit * this.units * this.agreedFX));
		pw.flush();
	}
	
	public boolean isIncoming() {
		return this.transactionType.equals("S");
	}
	
	public long getId() {
		return this.index;
	}
	
	public Date getDate() {
		return this.settlementDate;
	}
	
	public String getEntity() {
		return this.entity;
	}
	
	public float getValue() {
		return this.pricePerUnit * this.units * this.agreedFX;
	}
	
	private long generateRandomTransactionDate() {
		/*
		 * Generate a random transaction date in the range of today +- 1 week.
		 * The hour and minute values are constant for every date
		 */
		long randomDate = new Date().getTime()/3600000;
		randomDate = randomDate*3600000;
		randomDate = randomDate-7*24*3600000+rnd.nextInt(14)*24*3600000;
		return randomDate;
	}

	private boolean isSpecialCurrency (String currency) {
		return (currency.equals("AED")) || (currency.equals("SAR"));
	}
	
	private Date adjustSettlementDate(Date settlementDate) {
		/*
		 * adjusts the settlement date according to requirement
		 */
		Date adjustedSettlementDate = settlementDate;
		if (settlementDate.getDay() == 6) {
			adjustedSettlementDate = addTwoDays(settlementDate);
		} else if(settlementDate.getDay() == 0) {
			adjustedSettlementDate = addOneDay(settlementDate);
		}
		return adjustedSettlementDate;
	}

	private Date adjustSettlementDateForDifferentWorkDays(Date settlementDate) {
		/*
		 * adjusts the settlement date according to requirement for the special currencies
		 */
		Date adjustedSettlementDate = settlementDate;
		if (settlementDate.getDay() == 5) {
			adjustedSettlementDate = addTwoDays(settlementDate);
		} else if(settlementDate.getDay() == 6) {
			adjustedSettlementDate = addOneDay(settlementDate);
		}
		return adjustedSettlementDate;
	}
	
	private Date addOneDay(Date date) {
		return new Date(date.toInstant().plusSeconds(24*3600).toEpochMilli());
	}

	private Date addTwoDays(Date date) {
		return addOneDay(addOneDay(date));
	}

	private void incrementCounter() {
		counter++;
		this.index = counter;
	}

	@Override
	public int compareTo(Object arg0) {
		return (int) (this.settlementDate.getTime() - (long)(((Instruction)arg0).settlementDate.getTime()));
	}
	
	public static Comparator<Instruction> InstructionValueComparator = new Comparator<Instruction>() {

		public int compare(Instruction instruction1, Instruction instruction2) {
		//Sort the instructions by date and value
			Float instruction1Value = instruction1.agreedFX*instruction1.pricePerUnit*instruction1.units - instruction1.getDate().getTime()*1000000000;
			Float instruction2Value = instruction2.agreedFX*instruction2.pricePerUnit*instruction2.units - instruction2.getDate().getTime()*1000000000;
		
			//descending order
			return instruction2Value.compareTo(instruction1Value);
		
		}

	};

}
