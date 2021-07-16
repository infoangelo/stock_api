package pw.angelo.stock.builder;

import lombok.Builder;
import pw.angelo.stock.dto.StockDTO;
import pw.angelo.stock.enums.StockType;

@Builder
public class StockDTOBuilder {

    @Builder.Default
    private Long id = 1L;

    @Builder.Default
    private String name = "Brahma";

    @Builder.Default
    private String brand = "Ambev";

    @Builder.Default
    private int max = 50;

    @Builder.Default
    private int quantity = 10;

    @Builder.Default
    private StockType type = StockType.LAGER;

    public StockDTO toStockDTO() {
        return new StockDTO(id,
                name,
                brand,
                max,
                quantity,
                type);
    }
}
