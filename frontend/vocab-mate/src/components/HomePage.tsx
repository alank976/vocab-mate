import React from "react";
import { WordsInputBox } from "./WordsInputBox";

export const HomePage: React.FC<any> = () => (
  <div>
    <WordsInputBox
      id="words"
      label="Input your words below"
      instruction="Please separate words by newlines"
    />
  </div>
);
