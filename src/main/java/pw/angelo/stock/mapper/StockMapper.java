package pw.angelo.stock.mapper;

import pw.angelo.stock.dto.StockDTO;
import pw.angelo.stock.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StockMapper {

    StockMapper INSTANCE = Mappers.getMapper(StockMapper.class);

    Stock toModel(StockDTO stockDTO);

    StockDTO toDTO(Stock stock);
}
