package com.cartmatic.estoresearch.solr.index.impl;

import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.cartmatic.estore.Constants;
import com.cartmatic.estore.common.model.catalog.Category;
import com.cartmatic.estore.common.model.culturalinformation.CulturalInformation;
import com.cartmatic.estore.common.util.DateUtil;
import com.cartmatic.estore.core.exception.ApplicationException;
import com.cartmatic.estore.culturalinformation.service.CulturalInformationManager;
import com.cartmatic.estore.textsearch.SearchConstants;

public class CulturalInformationIndexBuildServiceImpl extends AbstractIndexBuildServiceImpl
{
    private SolrServer solrServer;
    private CulturalInformationManager culturalInformationManager;
    private static final Log logger = LogFactory.getLog(CulturalInformationIndexBuildServiceImpl.class);
    
    
    /**
     * 功能:初始化
     * <p>作者 杨荣忠 2015-6-30 上午11:14:10
     */
    public void init()
    {
        logger.info("CulturalInformationIndexBuildService init.");
        System.out.println("CulturalInformationIndexBuildService init.");
        solrServer = solrService.getSolrServer(SearchConstants.CORE_NAME_CULTURAL);
     
    }
    
    /**
     * 索引建立
     */
    @Override
    public void buildIndex(SearchConstants.UPDATE_TYPE type, List<Integer> ids, String hql)
    {  
    	
    	try
   		{
   			SolrServer solrServer2 = new CommonsHttpSolrServer("http://localhost:8080/solr/");
   		}
   		catch (MalformedURLException e)
   		{
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
   		
        if (SearchConstants.UPDATE_TYPE.REBUILD_ALL.equals(type))
        {
            rebuild();
            return;
        }
        if (ids != null)  //根据输入的主键
        {
            for (Integer pid : ids)
            {
                //processIndex(CulturalInformationManager.getById(pid), type);
                try
                {
                    if (SearchConstants.UPDATE_TYPE.UPDATE.equals(type))
                    {
                        solrServer.add(getDoc(culturalInformationManager.getById(pid)));
                      //  solrServer2.add(getDoc(culturalInformationManager.getById(pid)));
                    }
                    else if (SearchConstants.UPDATE_TYPE.DEL.equals(type))
                    {
                        solrServer.deleteById(pid.toString());
                    }
                }
                catch (Exception e)
                {
                    logger.error("Adding doc to solr is Failed.", e);
                }
            }
            solrService.flushChanges(solrServer, false);            
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Index was builded and going to be submit.");
        }
        solrService.flushChanges(solrServer, true);
    }
   
    
    /**
     * 功能:重建索引
     * <p>作者 杨荣忠 2015-6-30 上午11:13:40
     */
    private void rebuild()
    {
        try
        {
            removeAllIndexes(solrServer);
            addAllDoc(solrServer);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }
    
    //重新插入所有的索引
    private void addAllDoc(SolrServer server)
    {
        //状态是active
        String hql = "select p from CulturalInformation p where 1=1";
        List<CulturalInformation> rs = null;
        int page = 0;
        do
        {
            rs = appEventDao.fetchEntitysToProcess(hql, 50, page);
            page++;
            for (int i = 0; i < rs.size(); i++)
            {
                //processIndex(rs.get(i), SearchConstants.UPDATE_TYPE.UPDATE);
                try
                {
                    solrServer.add(getDoc(rs.get(i)));
                }
                catch (Exception e)
                {
                    logger.error("Adding doc to solr is Failed.", e);
                    e.printStackTrace();
                }                
            }
            try
            {
                solrService.flushChanges(solrServer, false);
            }
            catch(ApplicationException e)
            {
                logger.warn("Can not flushCaches.");
            }
        }
        while (rs != null && rs.size() == 50);
    }
    
    /**
     * 组合Document
     * @param vo
     * @return
     */
    private SolrInputDocument getDoc(CulturalInformation vo)
    {
        SolrInputDocument doc = new SolrInputDocument();
        //id
        doc.addField("id", vo.getId());
        doc.addField("title", vo.getTitle());
       // doc.addField("textIntroduction", vo.getCulturalInformationTitle(), 1.0f);
       //  doc.addField("releaseTime", vo.getr);
        doc.addField("writer", vo.getWriter());
        doc.addField("type", vo.getType());
        doc.addField("metaKeywork", vo.getMetaKeywork()); 
        return doc;
    }    
    
    
/*    
    //索引操作方法封装（更新与删除）
    private void processIndex(CulturalInformation vo, SearchConstants.UPDATE_TYPE indexType)
    {
        try
        {
            if (SearchConstants.UPDATE_TYPE.UPDATE.equals(indexType))
            {
                solrServer.add(getDoc(vo));
            }
            else if (SearchConstants.UPDATE_TYPE.DEL.equals(indexType))
            {
                solrServer.deleteById(vo.getId().toString());
            }
        }
        catch (Exception e)
        {
            logger.error("Adding doc to solr is Failed.", e);
        }
    }*/
    
    public void setCulturalInformationManager(CulturalInformationManager avalue)
    {
    	culturalInformationManager = avalue;
    }
}
