/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.gradle.attachment

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.TaskAction

/**
 * Detaches a project from this project.
 */
class DetachProjectTask extends DefaultTask {

    // name of the project being attached
    String projectName

    @Option(option = 'name',
            description = 'Name of the project being detached'
    )
    void setProjectName(String name) {
        projectName = name
    }

    @TaskAction
    void detachProject() {
        if (projectName == null) {
            throw new IllegalArgumentException('Must specify --name to detach a project')
        }
        File attachmentFile = new File(project.projectDir, ProjectAttachmentPlugin.ATTACHMENT_PREFIX + projectName)
        if (attachmentFile.exists() == false) {
            throw new GradleException("No attachment to '${projectName}' exists")
        }
        attachmentFile.delete()
    }
}
