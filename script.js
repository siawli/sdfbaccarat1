/* METHOD 1: USING AJAX TO READ LOCAL FILE DIRECTLY */
window.onload = () => {
  var table = document.getElementById("table");

  fetch("game_history.csv")
    .then((res) => res.text())
    .then((csv) => {
      // CLEAR HTML TABLE
      table.innerHTML = "";

      // SPLIT INTO ROWS
      let rows = csv.split("\n");

      // LOOP THROUGH ROWS AND SPLIT COLUMNS
      for (let row of rows) {
        let cols = row.match(/(?:\"([^\"]*(?:\"\"[^\"]*)*)\")|([^\",]+)/g);
        if (cols != null) {
          let tr = table.insertRow();
          for (let col of cols) {
            let td = tr.insertCell();
            td.innerHTML = col;
            if (col == "B") {
              td.style.color = "blue";
            } else if (col == "P") {
              td.style.color = "red";
            } else if (col == "D") {
              td.style.color = "green";
            } else {
              td.style.color = "black";
            }
          }
        }
      }
    });
};

/* METHOD 2: SELECT A FILE FROM POP-UP */
// window.onload = () => {
//   // FILE READER + HTML ELEMENTS
//   var reader = new FileReader(),
//     picker = document.getElementById("picker"),
//     table = document.getElementById("table");

//   // READ CSV ON FILE PICK
//   picker.onchange = () => reader.readAsText(picker.files[0]);

//   // READ CSV FILE AND GENERATE HTML
//   reader.onloadend = () => {
//     let csv = reader.result;

//     // CLEAR HTML TABLE
//     table.innerHTML = "";

//     // SPLIT INTO ROWS
//     let rows = csv.split("\n");

//     // LOOP THROUGH ROWS AND SPLIT COLUMNS
//     for (let row of rows) {
//       let cols = row.match(/(?:\"([^\"]*(?:\"\"[^\"]*)*)\")|([^\",]+)/g);
//       if (cols != null) {
//         let tr = table.insertRow();
//         for (let col of cols) {
//           let td = tr.insertCell();
//           td.innerHTML = col;
//         }
//       }
//     }
//   };
// };
