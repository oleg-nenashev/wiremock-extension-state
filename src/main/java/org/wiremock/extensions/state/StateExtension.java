/*
 * Copyright (C) 2023 Dirk Bolte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wiremock.extensions.state;

import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionFactory;
import com.github.tomakehurst.wiremock.extension.WireMockServices;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.store.Store;
import org.wiremock.extensions.state.extensions.DeleteStateEventListener;
import org.wiremock.extensions.state.extensions.RecordStateEventListener;
import org.wiremock.extensions.state.extensions.StateRequestMatcher;
import org.wiremock.extensions.state.extensions.StateTemplateHelperProviderExtension;
import org.wiremock.extensions.state.internal.ContextManager;

import java.util.Collections;
import java.util.List;

/**
 * Factory to register all extensions for handling state.
 * <p>
 * Register with:
 *
 * <pre>{@code
 *     private static final Store<String, Object> store = new CaffeineStore();
 *
 *     @RegisterExtension
 *     public static WireMockExtension wm = WireMockExtension.newInstance()
 *         .options(
 *             wireMockConfig().dynamicPort().dynamicHttpsPort()
 *                 .extensions(new StateExtension(store))
 *         )
 *         .build();
 *         }
 * </pre>
 */
public class StateExtension implements ExtensionFactory {

    private final StateTemplateHelperProviderExtension stateTemplateHelperProviderExtension;
    private final RecordStateEventListener recordStateEventListener;
    private final DeleteStateEventListener deleteStateEventListener;
    private final StateRequestMatcher stateRequestMatcher;

    public StateExtension(Store<String, Object> store) {
        var contextManager = new ContextManager(store);
        this.stateTemplateHelperProviderExtension = new StateTemplateHelperProviderExtension(contextManager);
        var templateEngine = new TemplateEngine(stateTemplateHelperProviderExtension.provideTemplateHelpers(), null, Collections.emptySet(), false);

        this.recordStateEventListener = new RecordStateEventListener(contextManager, templateEngine);
        this.deleteStateEventListener = new DeleteStateEventListener(contextManager, templateEngine);
        this.stateRequestMatcher = new StateRequestMatcher(contextManager, templateEngine);
    }

    @Override
    public List<Extension> create(WireMockServices services) {
        return List.of(
            recordStateEventListener,
            deleteStateEventListener,
            stateRequestMatcher,
            stateTemplateHelperProviderExtension
        );
    }
}
