package pw.angelo.stock.service;

import lombok.AllArgsConstructor;
import pw.angelo.stock.dto.StockDTO;
import pw.angelo.stock.entity.Stock;
import pw.angelo.stock.exception.StockAlreadyRegisteredException;
import pw.angelo.stock.exception.StockExceededException;
import pw.angelo.stock.exception.StockNotFoundException;
import pw.angelo.stock.mapper.StockMapper;
import pw.angelo.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper = StockMapper.INSTANCE;

    public StockDTO createStock(StockDTO stockDTO) throws StockAlreadyRegisteredException {
        verifyIfIsAlreadyRegistered(stockDTO.getName());
        Stock stock = stockMapper.toModel(stockDTO);
        Stock savedStock = stockRepository.save(stock);
        return stockMapper.toDTO(savedStock);
    }

    public StockDTO findByName(String name) throws StockNotFoundException {
        Stock foundStock = stockRepository.findByName(name)
                .orElseThrow(() -> new StockNotFoundException(name));
        return stockMapper.toDTO(foundStock);
    }

    public List<StockDTO> listAll() {
        return stockRepository.findAll()
                .stream()
                .map(stockMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) throws StockNotFoundException {
        verifyIfExists(id);
        stockRepository.deleteById(id);
    }

    private void verifyIfIsAlreadyRegistered(String name) throws StockAlreadyRegisteredException {
        Optional<Stock> optSavedStock = stockRepository.findByName(name);
        if (optSavedStock.isPresent()) {
            throw new StockAlreadyRegisteredException(name);
        }
    }

    private Stock verifyIfExists(Long id) throws StockNotFoundException {
        return stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException(id));
    }

    public StockDTO increment(Long id, int quantityToIncrement) throws StockNotFoundException, StockExceededException {
        Stock stockToIncrementStock = verifyIfExists(id);
        int quantityAfterIncrement = quantityToIncrement + stockToIncrementStock.getQuantity();
        if (quantityAfterIncrement <= stockToIncrementStock.getMax()) {
            stockToIncrementStock.setQuantity(stockToIncrementStock.getQuantity() + quantityToIncrement);
            Stock incrementedStockStock = stockRepository.save(stockToIncrementStock);
            return stockMapper.toDTO(incrementedStockStock);
        }
        throw new StockExceededException(id, quantityToIncrement);
    }
}
