package pw.angelo.stock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StockNotFoundException extends Exception {

    public StockNotFoundException(String beerName) {
        super(String.format("Stock with name %s not found in the system.", beerName));
    }

    public StockNotFoundException(Long id) {
        super(String.format("Stock with id %s not found in the system.", id));
    }
}
