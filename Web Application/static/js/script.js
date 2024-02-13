// parallax website scroll listener
let text = document.getElementById('text');
let leaf = document.getElementById('leaf');
let hill1 = document.getElementById('hill1');
let hill4 = document.getElementById('hill4');
let hill5 = document.getElementById('hill5');

window.addEventListener('scroll', () => {
    let value = window.scrollY;

    text.style.marginTop = value * 2.5 + 'px';
    leaf.style.top = value * -1.5 + 'px';
    leaf.style.left = value * 1.5 + 'px';
    hill5.style.left = value * 1.5 + 'px';
    hill4.style.left = value * -1.5 + 'px';
    hill1.style.top = value * 1 + 'px';
})

//preview image 
function previewImage(input) {
    const imageContainer = document.getElementById('image-container');
    const selectedImage = document.getElementById('selected-image');

    if (input.files && input.files[0]) {
        const reader = new FileReader();

        reader.onload = function (e) {
            selectedImage.src = e.target.result;
            selectedImage.style.display = 'block'; // Display the selected image
        };

        reader.readAsDataURL(input.files[0]);
    } else {
        // No file selected, hide the selected image
        selectedImage.style.display = 'none';
        selectedImage.src = ''; // Clear any previous image
    }
}







