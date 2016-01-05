# -*- coding: utf-8 -*-
"""
    Copyright 2011-2016 Asakusa Framework Team.
     
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
     
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    asakusafw.extlinks
    ~~~~~~~~~~~~~~~~~~~

    Extention to generate javadoc link to FQCN.
    
    This plugin based on ``sphinx.ext.extlinks``.

"""

from docutils import nodes, utils

from sphinx.util.nodes import split_explicit_title

import re

def make_link_role(base_url, prefix):
    def role(typ, rawtext, text, lineno, inliner, options={}, content=[]):
        text = utils.unescape(text)
        has_explicit_title, title, part = split_explicit_title(text)
        try:
            work = re.sub(r'\.([a-z])', r'/\1', part)
            work = re.sub(r'\.', '/', work, 1)
            full_url = base_url % work
        except (TypeError, ValueError):
            inliner.reporter.warning(
                'unable to expand %s extlink with base URL %r, please make '
                'sure the base contains \'%%s\' exactly once'
                % (typ, base_url), line=lineno)
            full_url = base_url + part
        if not has_explicit_title:
            if prefix is None:
                title = full_url
            else:
                title = prefix + part
        pnode = nodes.reference(title, title, internal=False, refuri=full_url)
        return [pnode], []
    return role

def setup_link_roles(app):
    for name, (base_url, prefix) in app.config.javadoclinks.iteritems():
        app.add_role(name, make_link_role(base_url, prefix))

def setup(app):
    app.add_config_value('javadoclinks', {}, 'env')
    app.connect('builder-inited', setup_link_roles)
