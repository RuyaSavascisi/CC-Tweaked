// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

/**
 * Find all HTML files generated by illuaminate and pipe them through a remark.
 *
 * This performs compile-time syntax highlighting and expands our custom
 * components using React SSR.
 *
 * Yes, this would be so much nicer with next.js.
 */
import * as fs from "fs/promises";
import { glob } from "glob";
import * as path from "path";
import { createElement as h } from 'react';
import runtime from 'react/jsx-runtime';
import { renderToStaticMarkup } from "react-dom/server";
import rehypeHighlight from "rehype-highlight";
import rehypeParse from 'rehype-parse';
import rehypeReact, { type Options as ReactOptions } from 'rehype-react';
import { unified } from 'unified';
// Our components
import Recipe from "./components/Recipe";
import { noChildren } from "./components/support";
import { type DataExport, WithExport } from "./components/WithExport";

(async () => {
    const base = "build/illuaminate";

    const reactOptions: ReactOptions = {
        ...(runtime as ReactOptions),
        components: {
            ['mc-recipe']: noChildren(Recipe),
            ['mcrecipe']: noChildren(Recipe),
            // Wrap example snippets in a <div class="lua-example">...</div>, so we can inject a
            // Run button into them.
            ['pre']: (args: JSX.IntrinsicElements["pre"] & { "data-lua-kind"?: undefined }) => {
                const element = <pre {...args} />;
                return args["data-lua-kind"] ? <div className="lua-example">{element}</div> : element
            }
        } as any
    };
    const processor = unified()
        .use(rehypeParse, { emitParseErrors: true })
        .use(rehypeHighlight, { prefix: "" })
        .use(rehypeReact, reactOptions);

    const dataExport = JSON.parse(await fs.readFile("src/export/index.json", "utf-8")) as DataExport;

    for (const file of await glob(base + "/**/*.html")) {
        const contents = await fs.readFile(file, "utf-8");

        const { result } = await processor.process(contents);

        const outputPath = path.resolve("build/jsxDocs", path.relative(base, file));
        await fs.mkdir(path.dirname(outputPath), { recursive: true });
        await fs.writeFile(outputPath, "<!doctype HTML>" + renderToStaticMarkup(<WithExport data={dataExport}>{result}</WithExport>));
    }
})();
