/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.slopeone;

import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.*;
import org.lenskit.data.ratings.Rating;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.PreferenceDomainBuilder;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.knn.item.model.ItemItemBuildContextProvider;
import org.grouplens.lenskit.transform.normalize.DefaultUserVectorNormalizer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class WeightedSlopeOneItemScorerTest {

    private static final double EPSILON = 1.0e-6;

    private SlopeOneModel getModel(EventDAO dao) {
        UserEventDAO uedao = new PrefetchingUserEventDAO(dao);
        ItemDAO idao = new PrefetchingItemDAO(dao);
        UserHistorySummarizer summarizer = new RatingVectorUserHistorySummarizer();
        ItemItemBuildContextProvider contextFactory = new ItemItemBuildContextProvider(
                uedao, new DefaultUserVectorNormalizer(), summarizer);
        SlopeOneModelBuilder provider = new SlopeOneModelBuilder(idao, contextFactory.get(), 0);
        return provider.get();
    }

    @Test
    public void testPredict1() throws RecommenderBuildException {

        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 6, 4));
        rs.add(Rating.create(2, 6, 2));
        rs.add(Rating.create(1, 7, 3));
        rs.add(Rating.create(2, 7, 2));
        rs.add(Rating.create(3, 7, 5));
        rs.add(Rating.create(4, 7, 2));
        rs.add(Rating.create(1, 8, 3));
        rs.add(Rating.create(2, 8, 4));
        rs.add(Rating.create(3, 8, 3));
        rs.add(Rating.create(4, 8, 2));
        rs.add(Rating.create(5, 8, 3));
        rs.add(Rating.create(6, 8, 2));
        rs.add(Rating.create(1, 9, 3));
        rs.add(Rating.create(3, 9, 4));

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(EventCollectionDAO.create(rs));
        config.bind(ItemScorer.class).to(WeightedSlopeOneItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomainBuilder(1, 5)
                                                       .setPrecision(1)
                                                       .build());
        ItemScorer predictor = LenskitRecommenderEngine.build(config)
                                                       .createRecommender()
                                                       .getItemScorer();

        assertThat(predictor, notNullValue());
        assertEquals(2.6, predictor.score(2, 9).getScore(), EPSILON);
        assertEquals(4.2, predictor.score(3, 6).getScore(), EPSILON);
        assertEquals(2, predictor.score(4, 6).getScore(), EPSILON);
        assertEquals(2, predictor.score(4, 9).getScore(), EPSILON);
        assertEquals(2.5, predictor.score(5, 6).getScore(), EPSILON);
        assertEquals(3, predictor.score(5, 7).getScore(), EPSILON);
        assertEquals(3.5, predictor.score(5, 9).getScore(), EPSILON);
        assertEquals(1.5, predictor.score(6, 6).getScore(), EPSILON);
        assertEquals(2, predictor.score(6, 7).getScore(), EPSILON);
        assertEquals(2.5, predictor.score(6, 9).getScore(), EPSILON);
    }

    @Test
    public void testPredict2() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 4, 3.5));
        rs.add(Rating.create(2, 4, 5));
        rs.add(Rating.create(3, 5, 4.25));
        rs.add(Rating.create(2, 6, 3));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(2, 7, 4));
        rs.add(Rating.create(3, 7, 1.5));

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(EventCollectionDAO.create(rs));
        config.bind(ItemScorer.class).to(WeightedSlopeOneItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomainBuilder(1, 5)
                                                       .setPrecision(1)
                                                       .build());
        ItemScorer predictor = LenskitRecommenderEngine.build(config)
                                                       .createRecommender()
                                                       .getItemScorer();

        assertThat(predictor, notNullValue());
        assertEquals(5, predictor.score(1, 5).getScore(), EPSILON);
        assertEquals(2.25, predictor.score(1, 6).getScore(), EPSILON);
        assertEquals(5, predictor.score(2, 5).getScore(), EPSILON);
        assertEquals(1.75, predictor.score(3, 4).getScore(), EPSILON);
        assertEquals(1, predictor.score(3, 6).getScore(), EPSILON);
    }
}