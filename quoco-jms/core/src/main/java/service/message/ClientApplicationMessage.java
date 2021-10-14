package service.message;

import java.util.List;

import java.io.Serializable;
import service.core.*;

public class ClientApplicationMessage implements Serializable{
    public long id;
    public List<Quotation> quotations;
    public ClientInfo info;
    public ClientApplicationMessage(long id, ClientInfo info, List<Quotation> quotations) {
        this.id = id;
        this.quotations = quotations;
        this.info = info;
    }
}
