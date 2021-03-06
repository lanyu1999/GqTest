package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.sellergoods.service.SpecificationService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = SpecificationService.class)
public class SpecificationServiceImpl extends BaseServiceImpl<TbSpecification> implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public List<Map<String, Object>> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

    @Override
    public void deleteSpecificationByIds(Long[] ids) {
        deleteByIds(ids);
        //批量删除规格
        Example example = new Example(TbSpecificationOption.class);
        example.createCriteria().andIn("specId", Arrays.asList(ids));
        specificationOptionMapper.deleteByExample(example);
    }

    @Override
    public void update(Specification specification) {
        //更新规格
        specificationMapper.updateByPrimaryKeySelective(specification.getSpecification());
        //删除原来的规格选项
      TbSpecificationOption param = new TbSpecificationOption();
      param.setSpecId(specification.getSpecification().getId());
      specificationOptionMapper.delete(param);
      //新增规格
        if(specification.getSpecificationOptionList() != null &&specification.getSpecificationOptionList().size()>0){
            for(TbSpecificationOption tbSpecificationOption :specification.getSpecificationOptionList()){
                tbSpecificationOption.setSpecId(specification.getSpecification().getId());
                specificationOptionMapper.insertSelective(tbSpecificationOption);
            }
        }
    }

    @Override
    public Specification findOne(Long id) {
        Specification specification = new Specification();
        specification.setSpecification(specificationMapper.selectByPrimaryKey(id));
        //查询并设置规格选项列表
        TbSpecificationOption param = new TbSpecificationOption();
        param.setSpecId(id);
        List<TbSpecificationOption> tbSpecificationOptionList = specificationOptionMapper.select(param);
        specification.setSpecificationOptionList(tbSpecificationOptionList);
        return specification;
    }

    @Override
    public void add(Specification specification) {
        specificationMapper.insertSelective(specification.getSpecification());
        if (specification.getSpecificationOptionList() != null && specification.getSpecificationOptionList().size() > 0) {
            for(TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()){
                tbSpecificationOption.setSpecId(specification.getSpecification().getId());
                specificationOptionMapper.insertSelective(tbSpecificationOption);
            }
        }
    }

    @Override
    public PageResult search(Integer page, Integer rows, TbSpecification specification) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(specification.getSpecName())) {
            criteria.andLike("specName", "%" + specification.getSpecName() + "%");
        }

        List<TbSpecification> list = specificationMapper.selectByExample(example);
        PageInfo<TbSpecification> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }
}
