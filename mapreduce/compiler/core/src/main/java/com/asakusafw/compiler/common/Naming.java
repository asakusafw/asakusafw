/**
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
package com.asakusafw.compiler.common;

import java.text.MessageFormat;

/**
 * Naming rule for Asakusa DSL.
 * @since 0.1.0
 * @version 0.2.6
 */
public final class Naming {

    /**
     * Returns the simple name of stage client class.
     * @return the simple name of stage client class
     */
    public static String getClientClass() {
        return "StageClient"; //$NON-NLS-1$
    }

    /**
     * Returns the simple name of mapper class.
     * @param inputId input port ID
     * @return the simple name of mapper class
     */
    public static String getMapClass(int inputId) {
        return String.format("%s%d", "StageMapper", inputId); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the simple name of reducer class.
     * @return the simple name of reducer class
     */
    public static String getReduceClass() {
        return "StageReducer"; //$NON-NLS-1$
    }

    /**
     * Returns the simple name of combiner class.
     * @return the simple name of combiner class
     */
    public static String getCombineClass() {
        return "StageCombiner"; //$NON-NLS-1$
    }

    /**
     * Returns the simple name of map fragment class.
     * @param serialNumber the serial number of the target fragment
     * @return the simple name of the target fragment class
     */
    public static String getMapFragmentClass(int serialNumber) {
        return String.format("%s%d", "MapFragment", serialNumber); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the simple name of reduce fragment class.
     * @param serialNumber the serial number of the target fragment
     * @return the simple name of the target fragment class
     */
    public static String getReduceFragmentClass(int serialNumber) {
        return String.format("%s%d", "ReduceFragment", serialNumber); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the simple name of map output fragment class.
     * @param serialNumber the serial number of the target fragment
     * @return the simple name of the target fragment class
     */
    public static String getMapOutputFragmentClass(int serialNumber) {
        return String.format("%s%d", "MapOutputFragment", serialNumber); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the simple name of combine output fragment class.
     * @param serialNumber the serial number of the target fragment
     * @return the simple name of the target fragment class
     */
    public static String getCombineOutputFragmentClass(int serialNumber) {
        return String.format("%s%d", "CombineOutputFragment", serialNumber); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the simple name of combine fragment class.
     * @param serialNumber the serial number of the target fragment
     * @return the simple name of the target fragment class
     */
    public static String getCombineFragmentClass(int serialNumber) {
        return String.format("%s%d", "CombineFragment", serialNumber); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the simple name of shuffle key class.
     * @return the simple name of the target class
     */
    public static String getShuffleKeyClass() {
        return "ShuffleKey"; //$NON-NLS-1$
    }

    /**
     * Returns the simple name of shuffle value class.
     * @return the simple name of the target class
     */
    public static String getShuffleValueClass() {
        return "ShuffleValue"; //$NON-NLS-1$
    }

    /**
     * Returns the simple name of partitioner class.
     * @return the simple name of the target class
     */
    public static String getShufflePartitionerClass() {
        return "ShufflePartitioner"; //$NON-NLS-1$
    }

    /**
     * Returns the simple name of grouping comparator class.
     * @return the simple name of the target class
     */
    public static String getShuffleGroupingComparatorClass() {
        return "ShuffleGroupingComparator"; //$NON-NLS-1$
    }

    /**
     * Returns the simple name of sort comparator class.
     * @return the simple name of the target class
     */
    public static String getShuffleSortComparatorClass() {
        return "ShuffleSortComparator"; //$NON-NLS-1$
    }

    /**
     * Returns the method name of copying shuffle keys.
     * @return the method name
     */
    public static String getShuffleKeyGroupCopier() {
        return "copyGroupFrom"; //$NON-NLS-1$
    }

    /**
     * The property name of grouping property of shuffle keys.
     * @param elementId the target element ID
     * @param termId the target term ID
     * @return the property name
     */
    public static String getShuffleKeyGroupProperty(int elementId, int termId) {
        return String.format("groupElem%dTerm%d", elementId, termId); //$NON-NLS-1$
    }

    /**
     * The property name of sort property of shuffle keys.
     * @param portId the target port ID
     * @param termId the target term ID
     * @return the property name
     */
    public static String getShuffleKeySortProperty(int portId, int termId) {
        return String.format("sortPort%dTerm%d", portId, termId); //$NON-NLS-1$
    }

    /**
     * The setter method name of shuffle key.
     * @param portId the target port ID
     * @return the method name
     */
    public static String getShuffleKeySetter(int portId) {
        return String.format("setPort%d", portId); //$NON-NLS-1$
    }

    /**
     * Returns the getter method name of shuffle value.
     * @param portId the target port ID
     * @return the method name
     */
    public static String getShuffleValueGetter(int portId) {
        return String.format("getPort%d", portId); //$NON-NLS-1$
    }

    /**
     * The setter method name of shuffle value.
     * @param portId the target port ID
     * @return the method name
     */
    public static String getShuffleValueSetter(int portId) {
        return String.format("setPort%d", portId); //$NON-NLS-1$
    }

    /**
     * Returns the stage name for main phase.
     * @param stageNumber the target stage number
     * @return the stage name
     */
    public static String getStageName(int stageNumber) {
        return String.format("stage%04d", stageNumber); //$NON-NLS-1$
    }

    /**
     * Returns the cleanup stage.
     * @return the stage name
     * @since 0.2.6
     */
    public static String getCleanupStageName() {
        return "cleanup"; //$NON-NLS-1$
    }

    /**
     * Returns the stage name for prologue phase.
     * @param moduleId the corresponding module ID
     * @return the stage name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static String getPrologueName(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return MessageFormat.format("prologue.{0}", moduleId); //$NON-NLS-1$
    }

    /**
     * Returns the stage name for prologue phase.
     * @param moduleId the corresponding module ID
     * @param stageId the stage ID
     * @return the stage name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static String getPrologueName(String moduleId, String stageId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return MessageFormat.format("prologue.{0}.{1}", moduleId, stageId); //$NON-NLS-1$
    }

    /**
     * Returns the stage name for epilogue phase.
     * @param moduleId the corresponding module ID
     * @return the stage name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static String getEpilogueName(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return MessageFormat.format("epilogue.{0}", moduleId); //$NON-NLS-1$
    }

    /**
     * Returns the stage name for epilogue phase.
     * @param moduleId the corresponding module ID
     * @param stageId the stage ID
     * @return the stage name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static String getEpilogueName(String moduleId, String stageId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return MessageFormat.format("epilogue.{0}.{1}", moduleId, stageId); //$NON-NLS-1$
    }

    /**
     * Returns the jobflow package file name.
     * @param flowId the target flow ID
     * @return the corresponded package file name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static String getJobflowClassPackageName(String flowId) {
        Precondition.checkMustNotBeNull(flowId, "flowId"); //$NON-NLS-1$
        return String.format("jobflow-%s%s", flowId, ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the jobflow source package file name.
     * @param flowId the target flow ID
     * @return the corresponded package file name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static String getJobflowSourceBundleName(String flowId) {
        Precondition.checkMustNotBeNull(flowId, "flowId"); //$NON-NLS-1$
        return String.format("jobflow-%s-sources%s", flowId, ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private Naming() {
        throw new AssertionError();
    }
}
