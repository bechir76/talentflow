import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach } from "vitest";
import i18n from "@/lib/i18n";

// Tests assert on the English texts.
void i18n.changeLanguage("en");

afterEach(() => cleanup());
