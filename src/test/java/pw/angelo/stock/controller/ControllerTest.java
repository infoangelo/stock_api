package pw.angelo.stock.controller;

import pw.angelo.stock.builder.StockDTOBuilder;
import pw.angelo.stock.dto.StockDTO;
import pw.angelo.stock.dto.QuantityDTO;
import pw.angelo.stock.exception.StockNotFoundException;
import pw.angelo.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import pw.angelo.stock.utils.JsonConvertionUtils;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ControllerTest {

    private static final String STOCK_API_URL_PATH = "/api/v1/stock";
    private static final long VALID_STOCK_ID = 1L;
    private static final long INVALID_STOCK_ID = 2l;
    private static final String STOCK_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String STOCK_API_SUBPATH_DECREMENT_URL = "/decrement";

    private MockMvc mockMvc;

    @Mock
    private StockService stockService;

    @InjectMocks
    private Controller controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Test
    void whenPOSTIsCalledThenAStockIsCreated() throws Exception {
        // given
        StockDTO stockDTO = StockDTOBuilder.builder().build().toStockDTO();

        // when
        when(stockService.createStock(stockDTO)).thenReturn(stockDTO);

        // then
        mockMvc.perform(post(STOCK_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString(stockDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(stockDTO.getName())))
                .andExpect(jsonPath("$.brand", is(stockDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(stockDTO.getType().toString())));
    }

    @Test
    void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorIsReturned() throws Exception {
        // given
        StockDTO stockDTO = StockDTOBuilder.builder().build().toStockDTO();
        stockDTO.setBrand(null);

        // then
        mockMvc.perform(post(STOCK_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString(stockDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {
        // given
        StockDTO stockDTO = StockDTOBuilder.builder().build().toStockDTO();

        //when
        when(stockService.findByName(stockDTO.getName())).thenReturn(stockDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(STOCK_API_URL_PATH + "/" + stockDTO.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(stockDTO.getName())))
                .andExpect(jsonPath("$.brand", is(stockDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(stockDTO.getType().toString())));
    }

    @Test
    void whenGETIsCalledWithoutRegisteredNameThenNotFoundStatusIsReturned() throws Exception {
        // given
        StockDTO stockDTO = StockDTOBuilder.builder().build().toStockDTO();

        //when
        when(stockService.findByName(stockDTO.getName())).thenThrow(StockNotFoundException.class);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(STOCK_API_URL_PATH + "/" + stockDTO.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGETListWithStockIsCalledThenOkStatusIsReturned() throws Exception {
        // given
        StockDTO stockDTO = StockDTOBuilder.builder().build().toStockDTO();

        //when
        when(stockService.listAll()).thenReturn(Collections.singletonList(stockDTO));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(STOCK_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(stockDTO.getName())))
                .andExpect(jsonPath("$[0].brand", is(stockDTO.getBrand())))
                .andExpect(jsonPath("$[0].type", is(stockDTO.getType().toString())));
    }

    @Test
    void whenGETListWithoutStockIsCalledThenOkStatusIsReturned() throws Exception {
        // given
        StockDTO stockDTO = StockDTOBuilder.builder().build().toStockDTO();

        //when
        when(stockService.listAll()).thenReturn(Collections.singletonList(stockDTO));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(STOCK_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenDELETEIsCalledWithValidIdThenNoContentStatusIsReturned() throws Exception {
        // given
        StockDTO stockDTO = StockDTOBuilder.builder().build().toStockDTO();

        //when
        doNothing().when(stockService).deleteById(stockDTO.getId());

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete(STOCK_API_URL_PATH + "/" + stockDTO.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDELETEIsCalledWithInvalidIdThenNotFoundStatusIsReturned() throws Exception {
        //when
        doThrow(StockNotFoundException.class).when(stockService).deleteById(INVALID_STOCK_ID);

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete(STOCK_API_URL_PATH + "/" + INVALID_STOCK_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHIsCalledToIncrementDiscountThenOKstatusIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(10)
                .build();

        StockDTO stockDTO = StockDTOBuilder.builder().build().toStockDTO();
        stockDTO.setQuantity(stockDTO.getQuantity() + quantityDTO.getQuantity());

        when(stockService.increment(VALID_STOCK_ID, quantityDTO.getQuantity())).thenReturn(stockDTO);

        mockMvc.perform(MockMvcRequestBuilders.patch(STOCK_API_URL_PATH + "/" + VALID_STOCK_ID + STOCK_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString(quantityDTO))).andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(stockDTO.getName())))
                .andExpect(jsonPath("$.brand", is(stockDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(stockDTO.getType().toString())))
                .andExpect(jsonPath("$.quantity", is(stockDTO.getQuantity())));
    }

}
