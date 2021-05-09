import { Story } from "@storybook/react/types-6-0";
import { WordsInputBox, WordsInputBoxProps } from "./WordsInputBox";

export default {
  componenet: WordsInputBox,
  title: "WordsInputBox",
};

const Template: Story<WordsInputBoxProps> = (args) => (
  <WordsInputBox {...args} />
);

export const Primary = Template.bind({});
Primary.args = {
  id: "words",
  label: "Put your words",
  instruction: "Please separate words by new lines"
};
