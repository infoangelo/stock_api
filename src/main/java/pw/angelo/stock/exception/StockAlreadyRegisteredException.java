package pw.angelo.stock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StockAlreadyRegisteredException extends Exception{

    public StockAlreadyRegisteredException(String beerName) {
        super(String.format("Stock with name %s already registered in the system.", beerName));
    }
}
