'use strict';

/*
 * ScamCheck MY web page (ADR-006): plain JavaScript, no build step.
 *
 * Safety rules for this file (NFR-05, enforced by StaticPageGuardTest):
 *  - Never use innerHTML, outerHTML, insertAdjacentHTML or eval. Everything the
 *    user pastes or the API returns is inserted as text only.
 *  - Never log or store the pasted text (ADR-008). Only the chosen language is
 *    remembered, in localStorage.
 */
(() => {
    const MAX_LENGTH = 5000;
    const LANGUAGES = ['ms', 'en'];
    const STORAGE_KEY = 'scamcheck.lang';
    const RISK_ICONS = {HIGH: '▲', MEDIUM: '◆', LOW: '●'};
    const STRENGTH_DOTS = {STRONG: '●●●', MODERATE: '●●○', WEAK: '●○○', INFO: '○○○'};
    // Missed-scam reports go to a public GitHub issue form (ADR-012). Nothing is
    // added to the URL: the user types the report there, after removing personal details.
    const REPORT_URL = 'https://github.com/emeraldex/scamcheck-my/issues/new?template=missed-scam.yml';

    const state = {lang: initialLanguage(), text: {}, lastResult: null, busy: false};

    const dom = {
        form: document.getElementById('check-form'),
        input: document.getElementById('text-input'),
        counter: document.getElementById('counter'),
        error: document.getElementById('form-error'),
        checkButton: document.getElementById('check-button'),
        exampleButton: document.getElementById('example-button'),
        result: document.getElementById('result'),
        langButtons: document.querySelectorAll('[data-lang]'),
    };

    // ---------- Language ----------

    function initialLanguage() {
        try {
            const saved = localStorage.getItem(STORAGE_KEY);
            if (LANGUAGES.includes(saved)) {
                return saved;
            }
        } catch (ignored) {
            // Storage can be blocked; fall back to the browser language.
        }
        const browser = ((navigator.languages && navigator.languages[0]) || navigator.language || '').toLowerCase();
        return browser.startsWith('en') ? 'en' : 'ms'; // BM is the fallback (US-09 AC1)
    }

    async function loadText(lang) {
        const response = await fetch(`i18n/${lang}.json`, {headers: {Accept: 'application/json'}});
        if (!response.ok) {
            throw new Error('Could not load UI text');
        }
        return response.json();
    }

    function t(key, vars = {}) {
        let value = state.text[key] ?? key;
        for (const [name, replacement] of Object.entries(vars)) {
            value = value.replaceAll(`{${name}}`, String(replacement));
        }
        return value;
    }

    function applyText() {
        document.documentElement.lang = state.lang;
        document.title = t('pageTitle');
        document.querySelectorAll('[data-i18n]').forEach(node => {
            node.textContent = t(node.dataset.i18n);
        });
        document.querySelectorAll('[data-i18n-placeholder]').forEach(node => {
            node.setAttribute('placeholder', t(node.dataset.i18nPlaceholder));
        });
        dom.langButtons.forEach(button => {
            button.setAttribute('aria-pressed', String(button.dataset.lang === state.lang));
        });
        if (state.busy) {
            dom.checkButton.textContent = t('checking');
        }
        if (!dom.error.hidden && dom.error.dataset.key) {
            dom.error.textContent = t(dom.error.dataset.key);
        }
        updateCounter();
    }

    async function setLanguage(lang) {
        if (lang === state.lang || !LANGUAGES.includes(lang)) {
            return;
        }
        try {
            state.text = await loadText(lang);
        } catch (ignored) {
            return; // keep the current language if the new text can't load
        }
        state.lang = lang;
        try {
            localStorage.setItem(STORAGE_KEY, lang);
        } catch (ignored) {
            // Not remembering the choice is fine.
        }
        applyText();
        // Explanations come from the API, so ask again in the new language.
        if (state.lastResult && dom.input.value.trim()) {
            await analyze();
        }
    }

    // ---------- Form ----------

    function updateCounter() {
        const length = dom.input.value.length;
        dom.counter.textContent = t('counter', {
            count: length.toLocaleString('en-US'),
            max: MAX_LENGTH.toLocaleString('en-US'),
        });
        dom.counter.classList.toggle('over-limit', length > MAX_LENGTH);
    }

    function showError(key) {
        dom.error.dataset.key = key;
        dom.error.textContent = t(key);
        dom.error.hidden = false;
    }

    function clearError() {
        dom.error.hidden = true;
        dom.error.textContent = '';
        delete dom.error.dataset.key;
    }

    function setBusy(busy) {
        state.busy = busy;
        dom.checkButton.disabled = busy;
        dom.checkButton.textContent = t(busy ? 'checking' : 'check');
        dom.result.setAttribute('aria-busy', String(busy));
    }

    async function analyze() {
        const text = dom.input.value;
        clearError();
        if (!text.trim()) {
            showError('errorEmpty'); // US-01 AC3
            dom.input.focus();
            return;
        }
        if (text.length > MAX_LENGTH) {
            showError('errorTooLong'); // US-01 AC4
            dom.input.focus();
            return;
        }

        setBusy(true);
        try {
            const response = await fetch('api/v1/analyze', {
                method: 'POST',
                headers: {'Content-Type': 'application/json', Accept: 'application/json'},
                body: JSON.stringify({text, language: state.lang}),
            });
            if (!response.ok) {
                showError(response.status === 400 ? 'errorInvalid' : 'errorServer');
                return;
            }
            state.lastResult = await response.json();
            renderResult(state.lastResult);
        } catch (ignored) {
            showError('errorNetwork');
        } finally {
            setBusy(false);
        }
    }

    // ---------- Result ----------

    /** Creates an element. Children are nodes or strings; strings always become text. */
    function h(tag, attributes = {}, ...children) {
        const node = document.createElement(tag);
        for (const [name, value] of Object.entries(attributes)) {
            if (name === 'class') {
                node.className = value;
            } else {
                node.setAttribute(name, value);
            }
        }
        for (const child of children.flat()) {
            if (child !== null && child !== undefined) {
                node.append(child);
            }
        }
        return node;
    }

    function helpLink(link) {
        const url = String(link.url || '');
        if (link.id === 'nsrc-997' && url.startsWith('tel:')) {
            return h('a', {class: 'btn btn-call', href: url},
                h('span', {'aria-hidden': 'true'}, '📞'), link.label);
        }
        if (url.startsWith('https://')) {
            return h('a', {class: 'btn btn-secondary', href: url, target: '_blank', rel: 'noopener noreferrer'},
                link.label, h('span', {'aria-hidden': 'true'}, '↗'),
                h('span', {class: 'visually-hidden'}, t('opensInNewTab')));
        }
        return null; // anything else is ignored rather than trusted
    }

    function findingItem(finding) {
        return h('li', {class: `finding weight-${finding.weight}`},
            h('div', {class: 'finding-head'},
                h('span', {class: 'strength', 'aria-hidden': 'true'}, STRENGTH_DOTS[finding.weight] || ''),
                h('h4', {}, finding.title)),
            h('span', {class: 'visually-hidden'}, t(`strength.${finding.weight}`)),
            h('p', {class: 'matched'},
                h('span', {class: 'visually-hidden'}, t('matchedText')),
                `“${finding.matchedText}”`),
            h('p', {}, finding.explanation));
    }

    function renderResult(data) {
        const level = ['HIGH', 'MEDIUM', 'LOW'].includes(data.riskLevel) ? data.riskLevel : 'LOW';
        const findings = Array.isArray(data.findings) ? data.findings : [];
        const links = (data.officialHelp && Array.isArray(data.officialHelp.links)) ? data.officialHelp.links : [];
        // The 997 call button comes first: it's the urgent action (WIREFRAME.md).
        const orderedLinks = [...links].sort((a, b) => (b.id === 'nsrc-997') - (a.id === 'nsrc-997'));

        const heading = h('h2', {id: 'result-heading', tabindex: '-1'},
            h('span', {'aria-hidden': 'true'}, RISK_ICONS[level]),
            t(`riskLabel.${level}`));

        const anotherButton = h('button', {type: 'button', class: 'link-button'}, t('checkAnother'));
        anotherButton.addEventListener('click', resetForm);

        const report = h('div', {class: 'report'},
            h('p', {class: 'report-prompt'}, t('reportPrompt'), ' ',
                h('a', {href: REPORT_URL, target: '_blank', rel: 'noopener noreferrer'},
                    t('reportLink'), h('span', {class: 'visually-hidden'}, ' ' + t('opensInNewTab')))),
            h('p', {class: 'report-note'}, t('reportNote')));

        const section = h('section', {class: `result risk-${level}`, 'aria-labelledby': 'result-heading'},
            h('div', {class: 'risk-banner'}, heading, h('p', {}, data.summary)),
            h('h3', {}, t('whatToDo')),
            h('p', {class: 'tip'}, data.officialHelp ? data.officialHelp.tip : ''),
            h('div', {class: 'help-links'}, orderedLinks.map(helpLink)),
            findings.length > 0 ? h('h3', {}, t('findingsHeading', {count: findings.length})) : null,
            findings.length > 0 ? h('ol', {class: 'findings'}, findings.map(findingItem)) : null,
            report,
            anotherButton);

        dom.result.replaceChildren(section);
        heading.focus({preventScroll: true});
        section.scrollIntoView({behavior: 'smooth', block: 'start'});
    }

    function resetForm() {
        state.lastResult = null;
        dom.result.replaceChildren();
        dom.input.value = '';
        clearError();
        updateCounter();
        window.scrollTo({top: 0, behavior: 'smooth'});
        dom.input.focus();
    }

    // ---------- Start ----------

    async function init() {
        try {
            state.text = await loadText(state.lang);
            applyText();
        } catch (ignored) {
            state.lang = 'ms'; // the page's built-in text is BM
        }

        dom.form.addEventListener('submit', event => {
            event.preventDefault();
            if (!state.busy) {
                analyze();
            }
        });
        dom.input.addEventListener('input', updateCounter);
        dom.input.addEventListener('keydown', event => {
            if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
                dom.form.requestSubmit();
            }
        });
        dom.exampleButton.addEventListener('click', () => {
            dom.input.value = t('exampleText'); // US-14
            updateCounter();
            dom.form.requestSubmit();
        });
        dom.langButtons.forEach(button => {
            button.addEventListener('click', () => setLanguage(button.dataset.lang));
        });
    }

    init();
})();
