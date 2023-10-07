package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.resolver.order.*;
import org.apache.hadoop.hdfs.server.federation.router.Router;
import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumMap;

// 就是在父类的基础上，多加了一步判断 destinations是否有多个，有多个的话 就用负载均衡器进行选择
public class MultipleDestinationMountTableResolver extends MountTableResolver {

    private static final Logger LOG = LoggerFactory.getLogger(MultipleDestinationMountTableResolver.class);


    private final EnumMap<DestinationOrder, OrderedResolver> orderedResolvers = new EnumMap<>(DestinationOrder.class);


    public MultipleDestinationMountTableResolver(
            Configuration conf, Router router) {
        super(conf, router);

        addResolver(DestinationOrder.LOCAL, new LocalResolver(conf, router));
        addResolver(DestinationOrder.SPACE, new AvailableSpaceResolver(conf, router));
        addResolver(DestinationOrder.RANDOM, new RandomResolver());
        addResolver(DestinationOrder.HASH, new HashFirstResolver());
        addResolver(DestinationOrder.HASH_ALL, new HashResolver());
    }

    @Override
    public PathLocation getDestinationForPath(String path) throws IOException {
        PathLocation mountTableResult = super.getDestinationForPath(path);
        if (mountTableResult == null) {
            LOG.error("The {} cannot find a location for {}",
                    super.getClass().getSimpleName(), path);
        } else if (mountTableResult.hasMultipleDestinations()) {
            DestinationOrder order = mountTableResult.getDestinationOrder();
            OrderedResolver orderedResolver = orderedResolvers.get(order);
            if (orderedResolver == null) {
                LOG.error("Cannot find resolver for order {}", order);
            } else {
                String firstNamespace =
                        orderedResolver.getFirstNamespace(path, mountTableResult);

                // Change the order of the name spaces according to the policy
                if (firstNamespace != null) {
                    // This is the entity in the tree, we need to create our own copy
                    mountTableResult = new PathLocation(mountTableResult, firstNamespace);
                    LOG.debug("Ordered locations following {} are {}", order, mountTableResult);
                } else {
                    LOG.error("Cannot get main namespace for path {} with order {}", path, order);
                }
            }
        }
        return mountTableResult;
    }

    @VisibleForTesting
    public void addResolver(DestinationOrder order, OrderedResolver resolver) {
        orderedResolvers.put(order, resolver);
    }
}