// Create UI elements
const numberInput1 = document.createElement("input");
numberInput1.id = "number1";
numberInput1.type = "number";

const numberInput2 = document.createElement("input");
numberInput2.id = "number2";
numberInput2.type = "number";

const addButton = document.createElement("button");
addButton.id = "addButton";
addButton.textContent = "Add";

const subtractButton = document.createElement("button");
subtractButton.id = "subtractButton";
subtractButton.textContent = "Subtract";

const resultElement = document.createElement("div");
resultElement.id = "result";

// Append UI elements to the DOM
document.body.appendChild(numberInput1);
document.body.appendChild(numberInput2);
document.body.appendChild(addButton);
document.body.appendChild(subtractButton);
document.body.appendChild(resultElement);

// Add event listeners to the buttons
addButton.addEventListener("click", () => {
  const num1 = Number(numberInput1.value);
  const num2 = Number(numberInput2.value);
  const sum = num1 + num2;
  resultElement.textContent = `Result: ${sum}`;
});

subtractButton.addEventListener("click", () => {
  const num1 = Number(numberInput1.value);
  const num2 = Number(numberInput2.value);
  const difference = num1 - num2;
  resultElement.textContent = `Result: ${difference}`;
});
