package pw.angelo.stock.controller;

import lombok.AllArgsConstructor;
import pw.angelo.stock.dto.StockDTO;
import pw.angelo.stock.dto.QuantityDTO;
import pw.angelo.stock.exception.StockAlreadyRegisteredException;
import pw.angelo.stock.exception.StockExceededException;
import pw.angelo.stock.exception.StockNotFoundException;
import pw.angelo.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class Controller implements ControllerDocs {

    private final StockService stockService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockDTO createBeer(@RequestBody @Valid StockDTO stockDTO) throws StockAlreadyRegisteredException {
        return stockService.createStock(stockDTO);
    }

    @GetMapping("/{name}")
    public StockDTO findByName(@PathVariable String name) throws StockNotFoundException {
        return stockService.findByName(name);
    }

    @GetMapping
    public List<StockDTO> listBeers() {
        return stockService.listAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) throws StockNotFoundException {
        stockService.deleteById(id);
    }

    @PatchMapping("/{id}/increment")
    public StockDTO increment(@PathVariable Long id, @RequestBody @Valid QuantityDTO quantityDTO) throws StockNotFoundException, StockExceededException {
        return stockService.increment(id, quantityDTO.getQuantity());
    }
}
