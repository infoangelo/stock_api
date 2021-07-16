package pw.angelo.stock.service;

import pw.angelo.stock.builder.StockDTOBuilder;
import pw.angelo.stock.dto.StockDTO;
import pw.angelo.stock.entity.Stock;
import pw.angelo.stock.exception.StockAlreadyRegisteredException;
import pw.angelo.stock.exception.StockExceededException;
import pw.angelo.stock.exception.StockNotFoundException;
import pw.angelo.stock.mapper.StockMapper;
import pw.angelo.stock.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

    private static final long INVALID_STOCK_ID = 1L;

    @Mock
    private StockRepository stockRepository;

    private StockMapper stockMapper = StockMapper.INSTANCE;

    @InjectMocks
    private StockService stockService;

    @Test
    void whenStockInformedThenItShouldBeCreated() throws StockAlreadyRegisteredException {
        // given
        StockDTO expectedStockDTO = StockDTOBuilder.builder().build().toStockDTO();
        Stock expectedSavedStock = stockMapper.toModel(expectedStockDTO);

        // when
        when(stockRepository.findByName(expectedStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.save(expectedSavedStock)).thenReturn(expectedSavedStock);

        //then
        StockDTO createdStockDTO = stockService.createStock(expectedStockDTO);

        assertThat(createdStockDTO.getId(), is(equalTo(expectedStockDTO.getId())));
        assertThat(createdStockDTO.getName(), is(equalTo(expectedStockDTO.getName())));
        assertThat(createdStockDTO.getQuantity(), is(equalTo(expectedStockDTO.getQuantity())));
    }

    @Test
    void whenAlreadyRegisteredStockInformedThenAnExceptionShouldBeThrown() {
        // given
        StockDTO expectedStockDTO = StockDTOBuilder.builder().build().toStockDTO();
        Stock duplicatedStock = stockMapper.toModel(expectedStockDTO);

        // when
        when(stockRepository.findByName(expectedStockDTO.getName())).thenReturn(Optional.of(duplicatedStock));

        // then
        assertThrows(StockAlreadyRegisteredException.class, () -> stockService.createStock(expectedStockDTO));
    }

    @Test
    void whenValidStockNameIsGivenThenReturnAStock() throws StockNotFoundException {
        // given
        StockDTO expectedFoundStockDTO = StockDTOBuilder.builder().build().toStockDTO();
        Stock expectedFoundStock = stockMapper.toModel(expectedFoundStockDTO);

        // when
        when(stockRepository.findByName(expectedFoundStock.getName())).thenReturn(Optional.of(expectedFoundStock));

        // then
        StockDTO foundStockDTO = stockService.findByName(expectedFoundStockDTO.getName());

        assertThat(foundStockDTO, is(equalTo(expectedFoundStockDTO)));
    }

    @Test
    void whenNotRegisteredStockNameIsGivenThenThrowAnException() {
        // given
        StockDTO expectedFoundStockDTO = StockDTOBuilder.builder().build().toStockDTO();

        // when
        when(stockRepository.findByName(expectedFoundStockDTO.getName())).thenReturn(Optional.empty());

        // then
        assertThrows(StockNotFoundException.class, () -> stockService.findByName(expectedFoundStockDTO.getName()));
    }

    @Test
    void whenListStockIsCalledThenReturnAListOfStock() {
        // given
        StockDTO expectedFoundStockDTO = StockDTOBuilder.builder().build().toStockDTO();
        Stock expectedFoundStock = stockMapper.toModel(expectedFoundStockDTO);

        //when
        when(stockRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundStock));

        //then
        List<StockDTO> foundListStockDTO = stockService.listAll();

        assertThat(foundListStockDTO, is(not(empty())));
        assertThat(foundListStockDTO.get(0), is(equalTo(expectedFoundStockDTO)));
    }

    @Test
    void whenListStockIsCalledThenReturnAnEmptyListOfStock() {
        //when
        when(stockRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

        //then
        List<StockDTO> foundListStockDTO = stockService.listAll();

        assertThat(foundListStockDTO, is(empty()));
    }

    @Test
    void whenExclusionIsCalledWithValidIdThenAStockShouldBeDeleted() throws StockNotFoundException {
        // given
        StockDTO expectedDeletedStockDTO = StockDTOBuilder.builder().build().toStockDTO();
        Stock expectedDeletedStock = stockMapper.toModel(expectedDeletedStockDTO);

        // when
        when(stockRepository.findById(expectedDeletedStockDTO.getId())).thenReturn(Optional.of(expectedDeletedStock));
        doNothing().when(stockRepository).deleteById(expectedDeletedStockDTO.getId());

        // then
        stockService.deleteById(expectedDeletedStockDTO.getId());

        verify(stockRepository, times(1)).findById(expectedDeletedStockDTO.getId());
        verify(stockRepository, times(1)).deleteById(expectedDeletedStockDTO.getId());
    }

    @Test
    void whenIncrementIsCalledThenIncrementStock() throws StockNotFoundException, StockExceededException {
        //given
        StockDTO expectedStockDTO = StockDTOBuilder.builder().build().toStockDTO();
        Stock expectedStock = stockMapper.toModel(expectedStockDTO);

        //when
        when(stockRepository.findById(expectedStockDTO.getId())).thenReturn(Optional.of(expectedStock));
        when(stockRepository.save(expectedStock)).thenReturn(expectedStock);

        int quantityToIncrement = 10;
        int expectedQuantityAfterIncrement = expectedStockDTO.getQuantity() + quantityToIncrement;

        // then
        StockDTO incrementedStockDTO = stockService.increment(expectedStockDTO.getId(), quantityToIncrement);

        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedStockDTO.getQuantity()));
        assertThat(expectedQuantityAfterIncrement, lessThan(expectedStockDTO.getMax()));
    }

    @Test
    void whenIncrementIsGreatherThanMaxThenThrowException() {
        StockDTO expectedStockDTO = StockDTOBuilder.builder().build().toStockDTO();
        Stock expectedStock = stockMapper.toModel(expectedStockDTO);

        when(stockRepository.findById(expectedStockDTO.getId())).thenReturn(Optional.of(expectedStock));

        int quantityToIncrement = 80;
        assertThrows(StockExceededException.class, () -> stockService.increment(expectedStockDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementAfterSumIsGreatherThanMaxThenThrowException() {
        StockDTO expectedStockDTO = StockDTOBuilder.builder().build().toStockDTO();
        Stock expectedStock = stockMapper.toModel(expectedStockDTO);

        when(stockRepository.findById(expectedStockDTO.getId())).thenReturn(Optional.of(expectedStock));

        int quantityToIncrement = 45;
        assertThrows(StockExceededException.class, () -> stockService.increment(expectedStockDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementIsCalledWithInvalidIdThenThrowException() {
        int quantityToIncrement = 10;

        when(stockRepository.findById(INVALID_STOCK_ID)).thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () -> stockService.increment(INVALID_STOCK_ID, quantityToIncrement));
    }
}
