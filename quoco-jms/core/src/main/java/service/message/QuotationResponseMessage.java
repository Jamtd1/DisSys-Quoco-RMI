package service.message;

import java.io.Serializable;
import service.core.*;

public class QuotationResponseMessage implements Serializable {
    public long id;
    public Quotation quotation;
    public QuotationResponseMessage(long id, Quotation quotation) {
        this.id = id;
        this.quotation = quotation;
    }
}