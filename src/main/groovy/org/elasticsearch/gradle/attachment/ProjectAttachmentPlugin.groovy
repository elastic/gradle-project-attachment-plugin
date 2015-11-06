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

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A plugin to allow attaching an external project.
 *
 * The attachment plugin adds tasks to attach and detach projects. A list
 * of attached project names is available as a property 'attachments'.
 */
class ProjectAttachmentPlugin implements Plugin<Project> {

    static final String ATTACHMENT_PREFIX = '.local-attachment.'

    static final String TASK_GROUP = 'Project Management'

    @Override
    void apply(Project project) {
        List attachments = []
        project.projectDir.eachFile { file ->
            if (file.getName().startsWith(ATTACHMENT_PREFIX)) {
                attachments.add(file.name.substring(ATTACHMENT_PREFIX.length()))
            }
        }
        project.ext.attachments = attachments

        // add projectsPrefix property so projects can do project dependencies
        for (String attachment : attachments) {
            String name = ":${attachment}"
            project.configure(project.subprojects.findAll { it.path.startsWith(name) }) {
                ext.projectsPrefix = name
            }
        }

        project.tasks.create(
                name: 'attach',
                type: AttachProjectTask,
                description: 'Attaches a project to this project.',
                group: TASK_GROUP)
        project.tasks.create(
                name: 'detach',
                type: DetachProjectTask,
                description: 'Detaches a project from this project.',
                group: TASK_GROUP)
    }
}
