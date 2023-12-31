// source: RouterProtocol.proto

package org.apache.hadoop.hdfs.protocol.proto;

public final class RouterProtocolProtos {
    private RouterProtocolProtos() {
    }

    public static void registerAllExtensions(
            org.apache.hadoop.thirdparty.protobuf.ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(
            org.apache.hadoop.thirdparty.protobuf.ExtensionRegistry registry) {
        registerAllExtensions(
                (org.apache.hadoop.thirdparty.protobuf.ExtensionRegistryLite) registry);
    }

    /**
     * Protobuf service {@code hadoop.hdfs.router.RouterAdminProtocolService}
     */
    public static abstract class RouterAdminProtocolService
            implements org.apache.hadoop.thirdparty.protobuf.Service {
        protected RouterAdminProtocolService() {
        }

        public interface Interface {
            /**
             * <pre>
             **
             * Add a mount table entry.
             * </pre>
             *
             * <code>rpc addMountTableEntry(.hadoop.hdfs.AddMountTableEntryRequestProto) returns (.hadoop.hdfs.AddMountTableEntryResponseProto);</code>
             */
            public abstract void addMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto> done);

            /**
             * <pre>
             **
             * Update an existing mount table entry without copying files.
             * </pre>
             *
             * <code>rpc updateMountTableEntry(.hadoop.hdfs.UpdateMountTableEntryRequestProto) returns (.hadoop.hdfs.UpdateMountTableEntryResponseProto);</code>
             */
            public abstract void updateMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto> done);

            /**
             * <pre>
             **
             * Remove a mount table entry.
             * </pre>
             *
             * <code>rpc removeMountTableEntry(.hadoop.hdfs.RemoveMountTableEntryRequestProto) returns (.hadoop.hdfs.RemoveMountTableEntryResponseProto);</code>
             */
            public abstract void removeMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto> done);

            /**
             * <pre>
             **
             * Get matching mount entries
             * </pre>
             *
             * <code>rpc getMountTableEntries(.hadoop.hdfs.GetMountTableEntriesRequestProto) returns (.hadoop.hdfs.GetMountTableEntriesResponseProto);</code>
             */
            public abstract void getMountTableEntries(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto> done);

            /**
             * <pre>
             **
             * Transform Router state to safe mode state.
             * </pre>
             *
             * <code>rpc enterSafeMode(.hadoop.hdfs.EnterSafeModeRequestProto) returns (.hadoop.hdfs.EnterSafeModeResponseProto);</code>
             */
            public abstract void enterSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto> done);

            /**
             * <pre>
             **
             * Transform Router state from safe mode to running state.
             * </pre>
             *
             * <code>rpc leaveSafeMode(.hadoop.hdfs.LeaveSafeModeRequestProto) returns (.hadoop.hdfs.LeaveSafeModeResponseProto);</code>
             */
            public abstract void leaveSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto> done);

            /**
             * <pre>
             **
             * Verify if current Router state is safe mode state.
             * </pre>
             *
             * <code>rpc getSafeMode(.hadoop.hdfs.GetSafeModeRequestProto) returns (.hadoop.hdfs.GetSafeModeResponseProto);</code>
             */
            public abstract void getSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto> done);

            /**
             * <pre>
             **
             * Disable a name service.
             * </pre>
             *
             * <code>rpc disableNameservice(.hadoop.hdfs.DisableNameserviceRequestProto) returns (.hadoop.hdfs.DisableNameserviceResponseProto);</code>
             */
            public abstract void disableNameservice(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto> done);

            /**
             * <pre>
             **
             * Enable a name service.
             * </pre>
             *
             * <code>rpc enableNameservice(.hadoop.hdfs.EnableNameserviceRequestProto) returns (.hadoop.hdfs.EnableNameserviceResponseProto);</code>
             */
            public abstract void enableNameservice(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto> done);

            /**
             * <pre>
             **
             * Get the list of disabled name services.
             * </pre>
             *
             * <code>rpc getDisabledNameservices(.hadoop.hdfs.GetDisabledNameservicesRequestProto) returns (.hadoop.hdfs.GetDisabledNameservicesResponseProto);</code>
             */
            public abstract void getDisabledNameservices(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto> done);

            /**
             * <pre>
             **
             * Refresh mount entries
             * </pre>
             *
             * <code>rpc refreshMountTableEntries(.hadoop.hdfs.RefreshMountTableEntriesRequestProto) returns (.hadoop.hdfs.RefreshMountTableEntriesResponseProto);</code>
             */
            public abstract void refreshMountTableEntries(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto> done);

            /**
             * <pre>
             **
             * Get the destination of a file/directory in the federation.
             * </pre>
             *
             * <code>rpc getDestination(.hadoop.hdfs.GetDestinationRequestProto) returns (.hadoop.hdfs.GetDestinationResponseProto);</code>
             */
            public abstract void getDestination(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto> done);

            /**
             * <pre>
             **
             * Refresh superuser proxy groups mappings on Router.
             * </pre>
             *
             * <code>rpc refreshSuperUserGroupsConfiguration(.hadoop.hdfs.RefreshSuperUserGroupsConfigurationRequestProto) returns (.hadoop.hdfs.RefreshSuperUserGroupsConfigurationResponseProto);</code>
             */
            public abstract void refreshSuperUserGroupsConfiguration(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto> done);

        }

        public static org.apache.hadoop.thirdparty.protobuf.Service newReflectiveService(
                final Interface impl) {
            return new RouterAdminProtocolService() {
                @java.lang.Override
                public void addMountTableEntry(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto> done) {
                    impl.addMountTableEntry(controller, request, done);
                }

                @java.lang.Override
                public void updateMountTableEntry(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto> done) {
                    impl.updateMountTableEntry(controller, request, done);
                }

                @java.lang.Override
                public void removeMountTableEntry(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto> done) {
                    impl.removeMountTableEntry(controller, request, done);
                }

                @java.lang.Override
                public void getMountTableEntries(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto> done) {
                    impl.getMountTableEntries(controller, request, done);
                }

                @java.lang.Override
                public void enterSafeMode(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto> done) {
                    impl.enterSafeMode(controller, request, done);
                }

                @java.lang.Override
                public void leaveSafeMode(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto> done) {
                    impl.leaveSafeMode(controller, request, done);
                }

                @java.lang.Override
                public void getSafeMode(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto> done) {
                    impl.getSafeMode(controller, request, done);
                }

                @java.lang.Override
                public void disableNameservice(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto> done) {
                    impl.disableNameservice(controller, request, done);
                }

                @java.lang.Override
                public void enableNameservice(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto> done) {
                    impl.enableNameservice(controller, request, done);
                }

                @java.lang.Override
                public void getDisabledNameservices(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto> done) {
                    impl.getDisabledNameservices(controller, request, done);
                }

                @java.lang.Override
                public void refreshMountTableEntries(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto> done) {
                    impl.refreshMountTableEntries(controller, request, done);
                }

                @java.lang.Override
                public void getDestination(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto> done) {
                    impl.getDestination(controller, request, done);
                }

                @java.lang.Override
                public void refreshSuperUserGroupsConfiguration(
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationRequestProto request,
                        org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto> done) {
                    impl.refreshSuperUserGroupsConfiguration(controller, request, done);
                }

            };
        }

        public static org.apache.hadoop.thirdparty.protobuf.BlockingService
        newReflectiveBlockingService(final BlockingInterface impl) {
            return new org.apache.hadoop.thirdparty.protobuf.BlockingService() {
                public final org.apache.hadoop.thirdparty.protobuf.Descriptors.ServiceDescriptor
                getDescriptorForType() {
                    return getDescriptor();
                }

                public final org.apache.hadoop.thirdparty.protobuf.Message callBlockingMethod(
                        org.apache.hadoop.thirdparty.protobuf.Descriptors.MethodDescriptor method,
                        org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                        org.apache.hadoop.thirdparty.protobuf.Message request)
                        throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException(
                                "Service.callBlockingMethod() given method descriptor for " +
                                        "wrong service type.");
                    }
                    switch (method.getIndex()) {
                        case 0:
                            return impl.addMountTableEntry(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryRequestProto) request);
                        case 1:
                            return impl.updateMountTableEntry(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryRequestProto) request);
                        case 2:
                            return impl.removeMountTableEntry(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto) request);
                        case 3:
                            return impl.getMountTableEntries(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto) request);
                        case 4:
                            return impl.enterSafeMode(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto) request);
                        case 5:
                            return impl.leaveSafeMode(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto) request);
                        case 6:
                            return impl.getSafeMode(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto) request);
                        case 7:
                            return impl.disableNameservice(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto) request);
                        case 8:
                            return impl.enableNameservice(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto) request);
                        case 9:
                            return impl.getDisabledNameservices(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto) request);
                        case 10:
                            return impl.refreshMountTableEntries(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto) request);
                        case 11:
                            return impl.getDestination(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto) request);
                        case 12:
                            return impl.refreshSuperUserGroupsConfiguration(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationRequestProto) request);
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

                public final org.apache.hadoop.thirdparty.protobuf.Message
                getRequestPrototype(
                        org.apache.hadoop.thirdparty.protobuf.Descriptors.MethodDescriptor method) {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException(
                                "Service.getRequestPrototype() given method " +
                                        "descriptor for wrong service type.");
                    }
                    switch (method.getIndex()) {
                        case 0:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryRequestProto.getDefaultInstance();
                        case 1:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryRequestProto.getDefaultInstance();
                        case 2:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto.getDefaultInstance();
                        case 3:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto.getDefaultInstance();
                        case 4:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto.getDefaultInstance();
                        case 5:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto.getDefaultInstance();
                        case 6:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto.getDefaultInstance();
                        case 7:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto.getDefaultInstance();
                        case 8:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto.getDefaultInstance();
                        case 9:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto.getDefaultInstance();
                        case 10:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto.getDefaultInstance();
                        case 11:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto.getDefaultInstance();
                        case 12:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationRequestProto.getDefaultInstance();
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

                public final org.apache.hadoop.thirdparty.protobuf.Message
                getResponsePrototype(
                        org.apache.hadoop.thirdparty.protobuf.Descriptors.MethodDescriptor method) {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException(
                                "Service.getResponsePrototype() given method " +
                                        "descriptor for wrong service type.");
                    }
                    switch (method.getIndex()) {
                        case 0:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto.getDefaultInstance();
                        case 1:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto.getDefaultInstance();
                        case 2:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto.getDefaultInstance();
                        case 3:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto.getDefaultInstance();
                        case 4:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto.getDefaultInstance();
                        case 5:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto.getDefaultInstance();
                        case 6:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto.getDefaultInstance();
                        case 7:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto.getDefaultInstance();
                        case 8:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto.getDefaultInstance();
                        case 9:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto.getDefaultInstance();
                        case 10:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto.getDefaultInstance();
                        case 11:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto.getDefaultInstance();
                        case 12:
                            return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto.getDefaultInstance();
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

            };
        }

        /**
         * <pre>
         **
         * Add a mount table entry.
         * </pre>
         *
         * <code>rpc addMountTableEntry(.hadoop.hdfs.AddMountTableEntryRequestProto) returns (.hadoop.hdfs.AddMountTableEntryResponseProto);</code>
         */
        public abstract void addMountTableEntry(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto> done);

        /**
         * <pre>
         **
         * Update an existing mount table entry without copying files.
         * </pre>
         *
         * <code>rpc updateMountTableEntry(.hadoop.hdfs.UpdateMountTableEntryRequestProto) returns (.hadoop.hdfs.UpdateMountTableEntryResponseProto);</code>
         */
        public abstract void updateMountTableEntry(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto> done);

        /**
         * <pre>
         **
         * Remove a mount table entry.
         * </pre>
         *
         * <code>rpc removeMountTableEntry(.hadoop.hdfs.RemoveMountTableEntryRequestProto) returns (.hadoop.hdfs.RemoveMountTableEntryResponseProto);</code>
         */
        public abstract void removeMountTableEntry(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto> done);

        /**
         * <pre>
         **
         * Get matching mount entries
         * </pre>
         *
         * <code>rpc getMountTableEntries(.hadoop.hdfs.GetMountTableEntriesRequestProto) returns (.hadoop.hdfs.GetMountTableEntriesResponseProto);</code>
         */
        public abstract void getMountTableEntries(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto> done);

        /**
         * <pre>
         **
         * Transform Router state to safe mode state.
         * </pre>
         *
         * <code>rpc enterSafeMode(.hadoop.hdfs.EnterSafeModeRequestProto) returns (.hadoop.hdfs.EnterSafeModeResponseProto);</code>
         */
        public abstract void enterSafeMode(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto> done);

        /**
         * <pre>
         **
         * Transform Router state from safe mode to running state.
         * </pre>
         *
         * <code>rpc leaveSafeMode(.hadoop.hdfs.LeaveSafeModeRequestProto) returns (.hadoop.hdfs.LeaveSafeModeResponseProto);</code>
         */
        public abstract void leaveSafeMode(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto> done);

        /**
         * <pre>
         **
         * Verify if current Router state is safe mode state.
         * </pre>
         *
         * <code>rpc getSafeMode(.hadoop.hdfs.GetSafeModeRequestProto) returns (.hadoop.hdfs.GetSafeModeResponseProto);</code>
         */
        public abstract void getSafeMode(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto> done);

        /**
         * <pre>
         **
         * Disable a name service.
         * </pre>
         *
         * <code>rpc disableNameservice(.hadoop.hdfs.DisableNameserviceRequestProto) returns (.hadoop.hdfs.DisableNameserviceResponseProto);</code>
         */
        public abstract void disableNameservice(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto> done);

        /**
         * <pre>
         **
         * Enable a name service.
         * </pre>
         *
         * <code>rpc enableNameservice(.hadoop.hdfs.EnableNameserviceRequestProto) returns (.hadoop.hdfs.EnableNameserviceResponseProto);</code>
         */
        public abstract void enableNameservice(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto> done);

        /**
         * <pre>
         **
         * Get the list of disabled name services.
         * </pre>
         *
         * <code>rpc getDisabledNameservices(.hadoop.hdfs.GetDisabledNameservicesRequestProto) returns (.hadoop.hdfs.GetDisabledNameservicesResponseProto);</code>
         */
        public abstract void getDisabledNameservices(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto> done);

        /**
         * <pre>
         **
         * Refresh mount entries
         * </pre>
         *
         * <code>rpc refreshMountTableEntries(.hadoop.hdfs.RefreshMountTableEntriesRequestProto) returns (.hadoop.hdfs.RefreshMountTableEntriesResponseProto);</code>
         */
        public abstract void refreshMountTableEntries(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto> done);

        /**
         * <pre>
         **
         * Get the destination of a file/directory in the federation.
         * </pre>
         *
         * <code>rpc getDestination(.hadoop.hdfs.GetDestinationRequestProto) returns (.hadoop.hdfs.GetDestinationResponseProto);</code>
         */
        public abstract void getDestination(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto> done);

        /**
         * <pre>
         **
         * Refresh superuser proxy groups mappings on Router.
         * </pre>
         *
         * <code>rpc refreshSuperUserGroupsConfiguration(.hadoop.hdfs.RefreshSuperUserGroupsConfigurationRequestProto) returns (.hadoop.hdfs.RefreshSuperUserGroupsConfigurationResponseProto);</code>
         */
        public abstract void refreshSuperUserGroupsConfiguration(
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationRequestProto request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto> done);

        public static final org.apache.hadoop.thirdparty.protobuf.Descriptors.ServiceDescriptor
        getDescriptor() {
            return org.apache.hadoop.hdfs.protocol.proto.RouterProtocolProtos.getDescriptor().getServices().get(0);
        }

        public final org.apache.hadoop.thirdparty.protobuf.Descriptors.ServiceDescriptor
        getDescriptorForType() {
            return getDescriptor();
        }

        public final void callMethod(
                org.apache.hadoop.thirdparty.protobuf.Descriptors.MethodDescriptor method,
                org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                org.apache.hadoop.thirdparty.protobuf.Message request,
                org.apache.hadoop.thirdparty.protobuf.RpcCallback<
                        org.apache.hadoop.thirdparty.protobuf.Message> done) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException(
                        "Service.callMethod() given method descriptor for wrong " +
                                "service type.");
            }
            switch (method.getIndex()) {
                case 0:
                    this.addMountTableEntry(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto>specializeCallback(
                                    done));
                    return;
                case 1:
                    this.updateMountTableEntry(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto>specializeCallback(
                                    done));
                    return;
                case 2:
                    this.removeMountTableEntry(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto>specializeCallback(
                                    done));
                    return;
                case 3:
                    this.getMountTableEntries(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto>specializeCallback(
                                    done));
                    return;
                case 4:
                    this.enterSafeMode(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto>specializeCallback(
                                    done));
                    return;
                case 5:
                    this.leaveSafeMode(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto>specializeCallback(
                                    done));
                    return;
                case 6:
                    this.getSafeMode(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto>specializeCallback(
                                    done));
                    return;
                case 7:
                    this.disableNameservice(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto>specializeCallback(
                                    done));
                    return;
                case 8:
                    this.enableNameservice(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto>specializeCallback(
                                    done));
                    return;
                case 9:
                    this.getDisabledNameservices(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto>specializeCallback(
                                    done));
                    return;
                case 10:
                    this.refreshMountTableEntries(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto>specializeCallback(
                                    done));
                    return;
                case 11:
                    this.getDestination(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto>specializeCallback(
                                    done));
                    return;
                case 12:
                    this.refreshSuperUserGroupsConfiguration(controller, (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationRequestProto) request,
                            org.apache.hadoop.thirdparty.protobuf.RpcUtil.<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto>specializeCallback(
                                    done));
                    return;
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public final org.apache.hadoop.thirdparty.protobuf.Message
        getRequestPrototype(
                org.apache.hadoop.thirdparty.protobuf.Descriptors.MethodDescriptor method) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException(
                        "Service.getRequestPrototype() given method " +
                                "descriptor for wrong service type.");
            }
            switch (method.getIndex()) {
                case 0:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryRequestProto.getDefaultInstance();
                case 1:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryRequestProto.getDefaultInstance();
                case 2:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto.getDefaultInstance();
                case 3:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto.getDefaultInstance();
                case 4:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto.getDefaultInstance();
                case 5:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto.getDefaultInstance();
                case 6:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto.getDefaultInstance();
                case 7:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto.getDefaultInstance();
                case 8:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto.getDefaultInstance();
                case 9:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto.getDefaultInstance();
                case 10:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto.getDefaultInstance();
                case 11:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto.getDefaultInstance();
                case 12:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationRequestProto.getDefaultInstance();
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public final org.apache.hadoop.thirdparty.protobuf.Message
        getResponsePrototype(
                org.apache.hadoop.thirdparty.protobuf.Descriptors.MethodDescriptor method) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException(
                        "Service.getResponsePrototype() given method " +
                                "descriptor for wrong service type.");
            }
            switch (method.getIndex()) {
                case 0:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto.getDefaultInstance();
                case 1:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto.getDefaultInstance();
                case 2:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto.getDefaultInstance();
                case 3:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto.getDefaultInstance();
                case 4:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto.getDefaultInstance();
                case 5:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto.getDefaultInstance();
                case 6:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto.getDefaultInstance();
                case 7:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto.getDefaultInstance();
                case 8:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto.getDefaultInstance();
                case 9:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto.getDefaultInstance();
                case 10:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto.getDefaultInstance();
                case 11:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto.getDefaultInstance();
                case 12:
                    return org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto.getDefaultInstance();
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public static Stub newStub(
                org.apache.hadoop.thirdparty.protobuf.RpcChannel channel) {
            return new Stub(channel);
        }

        public static final class Stub extends org.apache.hadoop.hdfs.protocol.proto.RouterProtocolProtos.RouterAdminProtocolService implements Interface {
            private Stub(org.apache.hadoop.thirdparty.protobuf.RpcChannel channel) {
                this.channel = channel;
            }

            private final org.apache.hadoop.thirdparty.protobuf.RpcChannel channel;

            public org.apache.hadoop.thirdparty.protobuf.RpcChannel getChannel() {
                return channel;
            }

            public void addMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(0),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto.getDefaultInstance()));
            }

            public void updateMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(1),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto.getDefaultInstance()));
            }

            public void removeMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(2),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto.getDefaultInstance()));
            }

            public void getMountTableEntries(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(3),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto.getDefaultInstance()));
            }

            public void enterSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(4),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto.getDefaultInstance()));
            }

            public void leaveSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(5),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto.getDefaultInstance()));
            }

            public void getSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(6),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto.getDefaultInstance()));
            }

            public void disableNameservice(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(7),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto.getDefaultInstance()));
            }

            public void enableNameservice(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(8),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto.getDefaultInstance()));
            }

            public void getDisabledNameservices(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(9),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto.getDefaultInstance()));
            }

            public void refreshMountTableEntries(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(10),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto.getDefaultInstance()));
            }

            public void getDestination(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(11),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto.getDefaultInstance()));
            }

            public void refreshSuperUserGroupsConfiguration(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationRequestProto request,
                    org.apache.hadoop.thirdparty.protobuf.RpcCallback<org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto> done) {
                channel.callMethod(
                        getDescriptor().getMethods().get(12),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto.getDefaultInstance(),
                        org.apache.hadoop.thirdparty.protobuf.RpcUtil.generalizeCallback(
                                done,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto.class,
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto.getDefaultInstance()));
            }
        }

        public static BlockingInterface newBlockingStub(
                org.apache.hadoop.thirdparty.protobuf.BlockingRpcChannel channel) {
            return new BlockingStub(channel);
        }

        public interface BlockingInterface {
            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto addMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto updateMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto removeMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto getMountTableEntries(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto enterSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto leaveSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto getSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto disableNameservice(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto enableNameservice(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto getDisabledNameservices(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto refreshMountTableEntries(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto getDestination(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto refreshSuperUserGroupsConfiguration(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException;
        }

        private static final class BlockingStub implements BlockingInterface {
            private BlockingStub(org.apache.hadoop.thirdparty.protobuf.BlockingRpcChannel channel) {
                this.channel = channel;
            }

            private final org.apache.hadoop.thirdparty.protobuf.BlockingRpcChannel channel;

            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto addMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(0),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.AddMountTableEntryResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto updateMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(1),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateMountTableEntryResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto removeMountTableEntry(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(2),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto getMountTableEntries(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(3),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto enterSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(4),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto leaveSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(5),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto getSafeMode(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(6),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto disableNameservice(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(7),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto enableNameservice(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(8),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto getDisabledNameservices(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(9),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto refreshMountTableEntries(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(10),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto getDestination(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(11),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto.getDefaultInstance());
            }


            public org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto refreshSuperUserGroupsConfiguration(
                    org.apache.hadoop.thirdparty.protobuf.RpcController controller,
                    org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationRequestProto request)
                    throws org.apache.hadoop.thirdparty.protobuf.ServiceException {
                return (org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto) channel.callBlockingMethod(
                        getDescriptor().getMethods().get(12),
                        controller,
                        request,
                        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto.getDefaultInstance());
            }

        }

        // @@protoc_insertion_point(class_scope:hadoop.hdfs.router.RouterAdminProtocolService)
    }


    public static org.apache.hadoop.thirdparty.protobuf.Descriptors.FileDescriptor
    getDescriptor() {
        return descriptor;
    }

    private static org.apache.hadoop.thirdparty.protobuf.Descriptors.FileDescriptor
            descriptor;

    static {
        java.lang.String[] descriptorData = {
                "\n\024RouterProtocol.proto\022\022hadoop.hdfs.rout" +
                        "er\032\030FederationProtocol.proto2\205\014\n\032RouterA" +
                        "dminProtocolService\022o\n\022addMountTableEntr" +
                        "y\022+.hadoop.hdfs.AddMountTableEntryReques" +
                        "tProto\032,.hadoop.hdfs.AddMountTableEntryR" +
                        "esponseProto\022x\n\025updateMountTableEntry\022.." +
                        "hadoop.hdfs.UpdateMountTableEntryRequest" +
                        "Proto\032/.hadoop.hdfs.UpdateMountTableEntr" +
                        "yResponseProto\022x\n\025removeMountTableEntry\022" +
                        "..hadoop.hdfs.RemoveMountTableEntryReque" +
                        "stProto\032/.hadoop.hdfs.RemoveMountTableEn" +
                        "tryResponseProto\022u\n\024getMountTableEntries" +
                        "\022-.hadoop.hdfs.GetMountTableEntriesReque" +
                        "stProto\032..hadoop.hdfs.GetMountTableEntri" +
                        "esResponseProto\022`\n\renterSafeMode\022&.hadoo" +
                        "p.hdfs.EnterSafeModeRequestProto\032\'.hadoo" +
                        "p.hdfs.EnterSafeModeResponseProto\022`\n\rlea" +
                        "veSafeMode\022&.hadoop.hdfs.LeaveSafeModeRe" +
                        "questProto\032\'.hadoop.hdfs.LeaveSafeModeRe" +
                        "sponseProto\022Z\n\013getSafeMode\022$.hadoop.hdfs" +
                        ".GetSafeModeRequestProto\032%.hadoop.hdfs.G" +
                        "etSafeModeResponseProto\022o\n\022disableNamese" +
                        "rvice\022+.hadoop.hdfs.DisableNameserviceRe" +
                        "questProto\032,.hadoop.hdfs.DisableNameserv" +
                        "iceResponseProto\022l\n\021enableNameservice\022*." +
                        "hadoop.hdfs.EnableNameserviceRequestProt" +
                        "o\032+.hadoop.hdfs.EnableNameserviceRespons" +
                        "eProto\022~\n\027getDisabledNameservices\0220.hado" +
                        "op.hdfs.GetDisabledNameservicesRequestPr" +
                        "oto\0321.hadoop.hdfs.GetDisabledNameservice" +
                        "sResponseProto\022\201\001\n\030refreshMountTableEntr" +
                        "ies\0221.hadoop.hdfs.RefreshMountTableEntri" +
                        "esRequestProto\0322.hadoop.hdfs.RefreshMoun" +
                        "tTableEntriesResponseProto\022c\n\016getDestina" +
                        "tion\022\'.hadoop.hdfs.GetDestinationRequest" +
                        "Proto\032(.hadoop.hdfs.GetDestinationRespon" +
                        "seProto\022\242\001\n#refreshSuperUserGroupsConfig" +
                        "uration\022<.hadoop.hdfs.RefreshSuperUserGr" +
                        "oupsConfigurationRequestProto\032=.hadoop.h" +
                        "dfs.RefreshSuperUserGroupsConfigurationR" +
                        "esponseProtoBC\n%org.apache.hadoop.hdfs.p" +
                        "rotocol.protoB\024RouterProtocolProtos\210\001\001\240\001" +
                        "\001"
        };
        org.apache.hadoop.thirdparty.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new org.apache.hadoop.thirdparty.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public org.apache.hadoop.thirdparty.protobuf.ExtensionRegistry assignDescriptors(
                            org.apache.hadoop.thirdparty.protobuf.Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        org.apache.hadoop.thirdparty.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new org.apache.hadoop.thirdparty.protobuf.Descriptors.FileDescriptor[]{
                                org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.getDescriptor(),
                        }, assigner);
        org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.getDescriptor();
    }

    // @@protoc_insertion_point(outer_class_scope)
}
