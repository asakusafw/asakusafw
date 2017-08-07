/*
 * Copyright 2011-2017 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.gradle.plugins


/**
 * An extension object for Asakusa SDK.
 * @since 0.9.0
 * @version 0.10.0
 */
class AsakusafwSdkExtension {

    /**
     * Whether or not the SDK core (including core runtime and vocabulary) is enabled.
     * This value will be resolved as {@code it as boolean}.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code true} </dd>
     * </dl>
     */
    Object core

    /**
     * Whether or not the DMDL compiler feature is enabled.
     * This value will be resolved as {@code it as boolean}.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code true} </dd>
     * </dl>
     */
    Object dmdl

    /**
     * Whether or not the operator DSL compiler feature is enabled.
     * This value will be resolved as {@code it as boolean}.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code true} </dd>
     * </dl>
     */
    Object operator

    /**
     * Whether or not the DSL testing feature is enabled.
     * This value will be resolved as {@code it as boolean}.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code true} </dd>
     * </dl>
     */
    Object testing

    /**
     * The symbol of active testkit.
     * This can be either {@link AsakusaTestkit}, {@link String} (symbolic),
     * {@code true} (the highest priority in the {@link #availableTestkits available testkit}),
     * or resolved as {@code false} (not available).
     * If the value is just {@code true}, the highest priority in {@link #availableTestkits} will be activated.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code true} </dd>
     * </dl>
     */
    Object testkit

    /**
     * Whether or not the YAESS feature is enabled.
     * This value will be resolved as {@code it as boolean}.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code true} </dd>
     * </dl>
     * @since 0.10.0
     */
    Object yaess

    /**
     * Whether or not Direct I/O feature is enabled.
     * This value will be resolved as {@code it as boolean}.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code true} </dd>
     * </dl>
     */
    Object directio

    /**
     * Whether or not WindGate feature is enabled.
     * This value will be resolved as {@code it as boolean}.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code false} </dd>
     * </dl>
     */
    Object windgate

    /**
     * Whether or not Direct I/O Hive support is enabled.
     * This value will be resolved as {@code it as boolean}.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code false} </dd>
     * </dl>
     */
    Object hive

    /**
     * The set available testkit.
     */
    Set<AsakusaTestkit> availableTestkits = new HashSet<>()

    /**
     * Whether or not incubating features are enabled.
     * This value will be resolved as {@code it as boolean}.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code false} </dd>
     * </dl>
     * @since 0.9.1
     */
    Object incubating
}
