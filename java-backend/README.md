This is the backend part of the application. The frontend part can be
found [here](https://github.com/Mateusz-Laczny/bpmn-ai-ui).

# Running locally with Docker

To run the application locally with Docker, Docker must be installed on your system.

To build the image, run:

```bash
docker build -t bpmn-ai-backend .
```

in the root directory of the repository.
To run the container, run:

```bash
docker run -p 8080:8080 bpmn-ai-backend
```

The application will be available at `localhost:3000`.

# Acknowledgments

This project includes a rewrite of a BPMN layouting algorithm; the original implementation can be
found [here](https://github.com/wojteklupin/bpmn-python-webapp).

The first attempt at layouting is an implementation of an algorithm by Kitzman et al.

```
Ingo Kitzmann et al. “A Simple Algorithm for Automatic Layout of BPMN Processes”.
In: 2009 IEEE Conference on Commerce and Enterprise Computing. 2009 IEEE Con-
ference on Commerce and Enterprise Computing. Vienna: IEEE, July 2009, pp. 391–
398. ISBN: 978-0-7695-3755-9. DOI: 10 . 1109 / CEC . 2009 . 28. URL: https :
//ieeexplore.ieee.org/document/5210767/
```
