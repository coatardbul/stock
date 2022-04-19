package com.coatardbul.stock.model.dto;

import com.coatardbul.stock.model.entity.StockOptionalPlate;
import com.coatardbul.stock.model.entity.StockOptionalPool;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/18
 *
 * @author Su Xiaolei
 */
@Data
public class PlateStockAddDTO {

    private StockOptionalPlate stockOptionalPlate;

    private List<StockOptionalPool> stockOptionalPoolList;

}
