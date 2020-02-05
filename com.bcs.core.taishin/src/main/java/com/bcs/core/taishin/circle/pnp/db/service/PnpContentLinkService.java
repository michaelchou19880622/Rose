package com.bcs.core.taishin.circle.pnp.db.service;

import com.bcs.core.taishin.circle.pnp.db.entity.PnpContentLink;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpContentLinkRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "PnpRecorder")
@Service
public class PnpContentLinkService {

    @Autowired
    private PnpContentLinkRepository contentLinkRepository;

    protected LoadingCache<String, PnpContentLink> dataCache; // No Need Sync

    public PnpContentLinkService() {

        dataCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, PnpContentLink>() {
                    @Override
                    public PnpContentLink load(String key) throws Exception {
                        return new PnpContentLink("-");
                    }
                });
    }

    @PreDestroy
    public void cleanUp() {
        log.info("[DESTROY] ContentLinkService cleaning up...");
        try {
            if (dataCache != null) {
                dataCache.invalidateAll();
                dataCache = null;
            }
        } catch (Exception e) {
        }

        System.gc();
        log.info("[DESTROY] ContentLinkService destroyed.");
    }

    private boolean notNull(PnpContentLink result) {
        if (result != null && StringUtils.isNotBlank(result.getLinkId()) && !"-".equals(result.getLinkId())) {
            return true;
        }
        return false;
    }

    /**
     * 取得所有連結清單
     */
    public List<Object[]> getAllContentLinkUrl() {
        return contentLinkRepository.findAllLinkUrl();
    }

    public List<Object[]> findAllLinkUrlByFlag(String flag) {
        return contentLinkRepository.findAllLinkUrlByFlag(flag);
    }

    public List<Object[]> findAllLinkUrlByLikeFlag(String flag) {
        return contentLinkRepository.findAllLinkUrlByLikeFlag(flag);
    }

    public List<Object[]> findAllLinkUrlByLikeTitle(String title) {
        return contentLinkRepository.findAllLinkUrlByLikeTitle(title);
    }

    public List<PnpContentLink> findAll() {
        return contentLinkRepository.findAll();
    }

    public Page<PnpContentLink> findAll(Pageable pageable) {
        return contentLinkRepository.findAll(pageable);
    }

    public List<PnpContentLink> findByLinkUrl(String linkUrl) {
        return contentLinkRepository.findByLinkUrl(linkUrl);
    }

    public List<PnpContentLink> findByLinkIdIn(List<String> linkIds) {
        return contentLinkRepository.findByLinkIdIn(linkIds);
    }

    public void save(PnpContentLink contentLink) {
        contentLinkRepository.save(contentLink);

        if (contentLink != null) {
            dataCache.put(contentLink.getLinkId(), contentLink);
        }
    }

    public void save(List<PnpContentLink> contentLinks) {
        for (PnpContentLink contentLink : contentLinks) {
            this.save(contentLink);
        }
    }

    public PnpContentLink findOne(String linkId) {
        try {
            PnpContentLink result = dataCache.get(linkId);
            if (notNull(result)) {
                return result;
            }
        } catch (Exception e) {
        }

        PnpContentLink result = contentLinkRepository.findOne(linkId);
        if (result != null) {
            dataCache.put(linkId, result);
        }
        return result;
    }

    public List<Object[]> countClickCountByLinkUrlAndTime(String linkUrl, String start, String end) {
        return contentLinkRepository.countClickCountByLinkUrlAndTime(linkUrl, start, end);
    }

    public List<Object[]> countClickCountByLinkUrl(String linkUrl) {
        return contentLinkRepository.countClickCountByLinkUrl(linkUrl);
    }

    public List<Object[]> countClickCountByLinkUrl(String linkUrl, String start) {
        return contentLinkRepository.countClickCountByLinkUrl(linkUrl, start);
    }

    public List<Object[]> countClickCountByLinkIdAndTime(String linkUrl, String start, String end) {
        return contentLinkRepository.countClickCountByLinkIdAndTime(linkUrl, start, end);
    }

    public List<Object[]> countClickCountByLinkId(String LinkId) {
        return contentLinkRepository.countClickCountByLinkId(LinkId);
    }

    public List<Object[]> countClickCountByLinkId(String LinkId, String start) {
        return contentLinkRepository.countClickCountByLinkId(LinkId, start);
    }

    public List<String> findClickMidByLinkUrlAndTime(String linkUrl, String start, String end) {
        return contentLinkRepository.findClickMidByLinkUrlAndTime(linkUrl, start, end);
    }
}

