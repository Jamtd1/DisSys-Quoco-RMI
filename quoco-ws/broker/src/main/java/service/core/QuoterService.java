package service.core;

import javax.jws.WebMethod;
import javax.jws.WebService;

// create WebService interface for the QuoterService
@WebService
public interface QuoterService {
    
    // webmethod to call the generateQuotation method of the quoters with the client info
    @WebMethod Quotation generateQuotation(ClientInfo info);
}