import React, { useState } from "react";
import { Input, Label, Tooltip } from "reactstrap";

export interface WordsInputBoxProps {
  id: string;
  label?: string;
  instruction?: string;
}
export const WordsInputBox: React.FC<WordsInputBoxProps> = ({
  id,
  label,
  instruction,
  ...props
}) => {
  let labelElement;
  if (label) {
    labelElement = <Label for={id}>{label}</Label>;
  }

  let tooltip;
  const [tooltipOpen, setTooltipOpen] = useState(false);
  const toggle = () => setTooltipOpen(!tooltipOpen);
  if (instruction) {
    tooltip = (
      <Tooltip
        placement="right"
        isOpen={tooltipOpen}
        target={id}
        toggle={toggle}
      >
        {instruction}
      </Tooltip>
    );
  }

  return (
    <div>
      <br />
      {labelElement}
      <br />
      <Input type="textarea" id={id} rows="10" />
      {tooltip}
    </div>
  );
};
