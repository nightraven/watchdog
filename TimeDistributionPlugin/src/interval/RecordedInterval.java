package interval;

import java.util.Date;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import document.IDocument;

public class RecordedInterval implements IInterval {

	private IDocument document;
	private Date start;
	private Date end;
	
	public RecordedInterval(IDocument document, Date start, Date end) {
		this.document = document;
		this.start = start;
		this.end = end;
	}

	@Override
	public IDocument getDocument() {
		return document;
	}
	
	@Override
	public Date getStart(){
		return start;
	}
	
	@Override
	public Date getEnd(){
		return end;
	}
	
	@Override 
	public Duration getDuration(){
		return new Duration(start.getTime(), end.getTime());
	}
	@Override 
	public String getDurationString(){
		Duration d = new Duration(start.getTime(), end.getTime());
		Period period = d.toPeriod();
		return PeriodFormat.getDefault().print(period);
	}

}
